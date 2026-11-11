#!/usr/bin/env python3
"""Render a self-contained HTML comparison from two JFR CPU recordings."""

import argparse
import html
import json
import subprocess
from collections import Counter
from pathlib import Path


def load_recording(path: Path):
    printed = subprocess.run(
        ["jfr", "print", "--json", "--events", "jdk.ExecutionSample", str(path)],
        check=True,
        capture_output=True,
        text=True,
    )
    events = json.loads(printed.stdout)["recording"]["events"]
    samples = []
    for event in events:
        trace = event.get("values", {}).get("stackTrace") or {}
        frames = trace.get("frames") or []
        names = []
        for frame in frames:
            method = frame.get("method") or {}
            owner = (method.get("type") or {}).get("name", "")
            name = method.get("name", "?")
            names.append(f"{owner.replace('/', '.')}.{name}" if owner else name)
        if names:
            # JFR prints the currently executing (leaf) frame first.
            samples.append(list(reversed(names)))

    summary = subprocess.run(
        ["jfr", "summary", str(path)], check=True, capture_output=True, text=True
    ).stdout
    duration = next(
        (line.split(":", 1)[1].strip() for line in summary.splitlines() if line.strip().startswith("Duration:")),
        "unknown",
    )
    return samples, duration


def svg_flamegraph(samples, title):
    width = 1200
    row_height = 22
    max_depth = min(24, max((len(sample) for sample in samples), default=1))
    height = (max_depth + 1) * row_height + 8
    root = {"name": "all samples", "count": 0, "children": {}}

    for sample in samples:
        node = root
        node["count"] += 1
        for name in sample[:max_depth]:
            node = node["children"].setdefault(name, {"name": name, "count": 0, "children": {}})
            node["count"] += 1

    elements = [f'<svg viewBox="0 0 {width} {height}" role="img" aria-label="{html.escape(title)} flame graph">']

    def draw(node, x, y, available_width):
        children = sorted(node["children"].values(), key=lambda item: item["count"], reverse=True)
        child_x = x
        for child in children:
            child_width = available_width * child["count"] / max(node["count"], 1)
            if child_width < 0.7:
                continue
            color = "#e7b45a" if "org.example.java_8" in child["name"] else "#7b8794"
            label = child["name"]
            elements.append(
                f'<g><title>{html.escape(label)} — {child["count"]} samples</title>'
                f'<rect x="{child_x:.2f}" y="{y}" width="{max(child_width - 1, 0):.2f}" '
                f'height="{row_height - 1}" rx="2" fill="{color}"/>'
            )
            if child_width > 80:
                elements.append(
                    f'<text x="{child_x + 4:.2f}" y="{y + 15}" class="frame-label">'
                    f'{html.escape(label[:max(8, int(child_width / 7))])}</text>'
                )
            elements.append("</g>")
            draw(child, child_x, y + row_height, child_width)
            child_x += child_width

    draw(root, 0, 4, width)
    elements.append("</svg>")
    return "".join(elements)


def hot_methods(samples, limit=12):
    counts = Counter(sample[-1] for sample in samples if sample)
    if not counts:
        return "<p>No CPU execution samples were recorded.</p>"
    rows = "".join(
        f"<tr><td><code>{html.escape(method)}</code></td><td>{count}</td></tr>"
        for method, count in counts.most_common(limit)
    )
    return f"<table><thead><tr><th>Sampled method</th><th>Samples</th></tr></thead><tbody>{rows}</tbody></table>"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--nplus1", type=Path, required=True)
    parser.add_argument("--optimized", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--run-id", required=True)
    parser.add_argument("--requests", type=int, required=True)
    args = parser.parse_args()

    nplus_samples, nplus_duration = load_recording(args.nplus1)
    opt_samples, opt_duration = load_recording(args.optimized)
    nplus_link = html.escape(args.nplus1.relative_to(args.output.parent).as_posix())
    opt_link = html.escape(args.optimized.relative_to(args.output.parent).as_posix())

    sections = []
    for name, samples, duration, link in (
        ("N+1", nplus_samples, nplus_duration, nplus_link),
        ("Optimized", opt_samples, opt_duration, opt_link),
    ):
        sections.append(
            f"<section><h2>{name}</h2><p>{args.requests:,} requests · {len(samples)} CPU samples · {html.escape(duration)}</p>"
            f"<p><a href=\"{link}\">Open JFR recording</a></p>"
            f"<div class=\"graph\">{svg_flamegraph(samples, name)}</div>"
            f"<h3>Hot sampled methods</h3>{hot_methods(samples)}</section>"
        )

    document = f"""<!doctype html>
<html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Endpoint CPU profile comparison</title><style>
body{{font:15px system-ui,sans-serif;max-width:1300px;margin:32px auto;padding:0 20px;color:#e5e7eb;background:#111827}}
h1,h2{{color:#fff}}section{{margin:28px 0;padding:22px;background:#1f2937;border-radius:10px}}
.graph{{overflow-x:auto;background:#0b1220;padding:8px;border-radius:6px}}svg{{width:100%;min-width:800px;height:auto}}
.frame-label{{font:11px system-ui,sans-serif;fill:#111827;pointer-events:none}}table{{border-collapse:collapse;width:100%}}
th,td{{text-align:left;padding:7px;border-bottom:1px solid #374151}}a{{color:#93c5fd}}code{{font-size:12px;overflow-wrap:anywhere}}
.note{{color:#cbd5e1}}
</style></head><body><h1>Endpoint CPU profile comparison</h1>
<p class="note">Run {html.escape(args.run_id)}. Recordings start after app startup, seeding, and warm-up. Flame graphs aggregate JFR CPU stack samples; they are not SQL-query-count charts.</p>
{''.join(sections)}
</body></html>"""
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(document, encoding="utf-8")
    print(f"Wrote browser report: {args.output}")


if __name__ == "__main__":
    main()
