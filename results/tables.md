**LCS and Edit Distance** (average of 5 runs)

|    n | LCS time (µs) | Edit Distance time (µs) | DP cells filled (n²) |
|-----:|--------------:|------------------------:|---------------------:|
|  100 |          81.3 |                    82.9 |                10000 |
|  500 |        2199.4 |                  1997.3 |               250000 |
| 1000 |        9105.1 |                 10244.9 |              1000000 |

**String matching, time** (average of 5 runs, pattern length 20)

| Text length | Pattern | Naive (µs) | KMP (µs) | Rabin–Karp (µs) | Occurrences |
|------------:|---------|-----------:|---------:|----------------:|------------:|
|       10000 | present |      135.3 |     62.5 |           163.8 |           1 |
|       10000 | absent  |       71.7 |     99.3 |           127.3 |           0 |
|       50000 | present |      544.3 |    481.8 |           636.7 |           1 |
|       50000 | absent  |      576.8 |    443.6 |           617.1 |           0 |
|      100000 | present |      907.7 |    902.9 |          1365.1 |           1 |
|      100000 | absent  |      921.6 |    941.9 |          1248.6 |           0 |

**String matching, operation counts** (character comparisons, one run)

| Text length | Pattern |  Naive | KMP (incl. LPS build) | Rabin–Karp (verification only) | Rabin–Karp false hash matches |
|------------:|---------|-------:|----------------------:|-------------------------------:|------------------------------:|
|       10000 | present |  13353 |                 12471 |                             20 |                             0 |
|       10000 | absent  |  13283 |                 12559 |                              0 |                             0 |
|       50000 | present |  66588 |                 61685 |                             20 |                             0 |
|       50000 | absent  |  66547 |                 62476 |                              0 |                             0 |
|      100000 | present | 132854 |                124298 |                             20 |                             0 |
|      100000 | absent  | 133113 |                124785 |                              0 |                             0 |

**Kasai's algorithm** (average of 5 runs; suffix array built beforehand, not timed)

|     n |  Time (µs) | Character comparisons |
|------:|-----------:|----------------------:|
|  1000 |       99.7 |                  1990 |
|  5000 |      314.6 |                  9988 |
| 10000 |      277.9 |                 19988 |
