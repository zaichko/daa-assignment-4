"""Reads results/*.csv, writes plots/*.png and results/tables.md.

Usage (from the repository root):  python3 scripts/plot.py
Requires matplotlib.
"""
import csv
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt


def read(path):
    with open(path, newline="") as f:
        return list(csv.DictReader(f))


dp = read("results/dp.csv")
match = read("results/matching.csv")
kasai = read("results/kasai.csv")
os.makedirs("plots", exist_ok=True)

# ---- Plot 1: LCS and Edit Distance -----------------------------------------
fig, ax = plt.subplots(figsize=(6, 4))
for algo in ("LCS", "EditDistance"):
    rows = [r for r in dp if r["algorithm"] == algo]
    ax.plot([int(r["n"]) for r in rows], [float(r["avg_us"]) / 1000 for r in rows],
            marker="o", label=algo)
ax.set_xlabel("String length n (both strings)")
ax.set_ylabel("Average time (ms)")
ax.set_title("LCS and Edit Distance: time vs input size")
ax.grid(True, alpha=0.3)
ax.legend()
fig.tight_layout()
fig.savefig("plots/dp_time.png", dpi=150)
plt.close(fig)

# ---- Plot 2: string matching -----------------------------------------------
fig, axes = plt.subplots(1, 2, figsize=(10, 4), sharey=True)
for ax, pat in zip(axes, ("present", "absent")):
    for algo in ("Naive", "KMP", "RabinKarp"):
        rows = [r for r in match if r["algorithm"] == algo and r["pattern"] == pat]
        ax.plot([int(r["text_len"]) for r in rows], [float(r["avg_us"]) for r in rows],
                marker="o", label=algo)
    ax.set_title(f"Pattern {pat} in text")
    ax.set_xlabel("Text length")
    ax.grid(True, alpha=0.3)
axes[0].set_ylabel("Average time (µs)")
axes[0].legend()
fig.tight_layout()
fig.savefig("plots/matching_time.png", dpi=150)
plt.close(fig)

# ---- Plot 3 (extra): Kasai ---------------------------------------------------
fig, ax = plt.subplots(figsize=(6, 4))
ax.plot([int(r["n"]) for r in kasai], [float(r["avg_us"]) for r in kasai], marker="o")
ax.set_xlabel("String length n")
ax.set_ylabel("Average time (µs)")
ax.set_title("Kasai's LCP construction: time vs input size")
ax.grid(True, alpha=0.3)
fig.tight_layout()
fig.savefig("plots/kasai_time.png", dpi=150)
plt.close(fig)

# ---- Markdown tables ---------------------------------------------------------
out = []
out.append("**LCS and Edit Distance** (average of 5 runs)\n")
out.append("| n | LCS time (µs) | Edit Distance time (µs) | DP cells filled (n²) |")
out.append("|---:|---:|---:|---:|")
for n in sorted({int(r["n"]) for r in dp}):
    lcs = next(r for r in dp if r["algorithm"] == "LCS" and int(r["n"]) == n)
    ed = next(r for r in dp if r["algorithm"] == "EditDistance" and int(r["n"]) == n)
    out.append(f"| {n} | {float(lcs['avg_us']):.1f} | {float(ed['avg_us']):.1f} | {lcs['cell_updates']} |")

out.append("\n**String matching, time** (average of 5 runs, pattern length 20)\n")
out.append("| Text length | Pattern | Naive (µs) | KMP (µs) | Rabin–Karp (µs) | Occurrences |")
out.append("|---:|---|---:|---:|---:|---:|")
combos = sorted({(int(r["text_len"]), r["pattern"]) for r in match}, key=lambda c: (c[0], c[1] != "present"))
for n, pat in combos:
    def get(a):
        return next(r for r in match if r["algorithm"] == a and int(r["text_len"]) == n and r["pattern"] == pat)
    nv, km, rk = get("Naive"), get("KMP"), get("RabinKarp")
    out.append(f"| {n} | {pat} | {float(nv['avg_us']):.1f} | {float(km['avg_us']):.1f} | "
               f"{float(rk['avg_us']):.1f} | {nv['occurrences']} |")

out.append("\n**String matching, operation counts** (character comparisons, one run)\n")
out.append("| Text length | Pattern | Naive | KMP (incl. LPS build) | Rabin–Karp (verification only) | Rabin–Karp false hash matches |")
out.append("|---:|---|---:|---:|---:|---:|")
for n, pat in combos:
    def get(a):
        return next(r for r in match if r["algorithm"] == a and int(r["text_len"]) == n and r["pattern"] == pat)
    nv, km, rk = get("Naive"), get("KMP"), get("RabinKarp")
    out.append(f"| {n} | {pat} | {nv['char_comparisons']} | {km['char_comparisons']} | "
               f"{rk['char_comparisons']} | {rk['rk_false_matches']} |")

out.append("\n**Kasai's algorithm** (average of 5 runs; suffix array built beforehand, not timed)\n")
out.append("| n | Time (µs) | Character comparisons |")
out.append("|---:|---:|---:|")
for r in kasai:
    out.append(f"| {r['n']} | {float(r['avg_us']):.1f} | {r['char_comparisons']} |")

with open("results/tables.md", "w", encoding="utf-8") as f:
    f.write("\n".join(out) + "\n")
print("wrote plots/*.png and results/tables.md")
