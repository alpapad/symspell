# symspell

This is java port of https://github.com/wolfgarbe/SymSpell , Spelling correction & Fuzzy search: **1 million times faster** through Symmetric Delete spelling correction algorithm

The code is modified for better java performance. 

Two versions of SymSpell have been ported:

 - Version 3 of SymSpell, adapted to use a different index
 - Version 6 of SymSpell, with minimum changes (used to benchmark the adaptions for v3)
 
        
        Precalculation v3          2s    16.14Mb     29,159 words    848,517 entries  MaxEditDistance=2 dict=30k
        Precalculation v6          1s    33.87Mb     29,159 words    848,518 entries  MaxEditDistance=2 dict=30k
        lookup v3          9 results  59,997.79ns/op verbosity=All query=exact
        lookup v6          9 results  51,153.73ns/op verbosity=All query=exact

        lookup v3          3 results  31,084.49ns/op verbosity=All query=non-exact
        lookup v6          3 results  33,904.03ns/op verbosity=All query=non-exact

        lookup v3      2,078 results 132,668.49ns/op verbosity=All query=mix
        lookup v6      2,240 results  76,242.04ns/op verbosity=All query=mix


        Precalculation v3         13s    61.51Mb     82,765 words  2,562,642 entries  MaxEditDistance=2 dict=82k
        Precalculation v6          6s   114.48Mb     82,765 words  2,562,643 entries  MaxEditDistance=2 dict=82k
        lookup v3         11 results  42,390.96ns/op verbosity=All query=exact
        lookup v6         11 results  56,567.40ns/op verbosity=All query=exact

        lookup v3         32 results  34,121.17ns/op verbosity=All query=non-exact
        lookup v6         32 results  35,686.58ns/op verbosity=All query=non-exact

        lookup v3      3,358 results  79,759.16ns/op verbosity=All query=mix
        lookup v6      3,388 results 345,096.28ns/op verbosity=All query=mix


        Precalculation v3        138s   278.01Mb    496,508 words 12,496,304 entries  MaxEditDistance=2 dict=500k
        Precalculation v6         40s   528.21Mb    496,508 words 12,496,305 entries  MaxEditDistance=2 dict=500k
        lookup v3         60 results  81,566.63ns/op verbosity=All query=exact
        lookup v6         60 results  79,320.98ns/op verbosity=All query=exact

        lookup v3        115 results 110,983.30ns/op verbosity=All query=non-exact
        lookup v6        115 results 158,891.41ns/op verbosity=All query=non-exact

        lookup v3     22,917 results 235,408.28ns/op verbosity=All query=mix
        lookup v6     23,877 results 464,849.42ns/op verbosity=All query=mix


        Average Precalculation time v3      51.00s   225.53%
        Average Precalculation time v6      15.67s
        Average Precalculation memory v3   118.55Mb -47.43%
        Average Precalculation memory v6   225.52Mb 
        Average lookup time v3  62,497.32ns          -17.24%
        Average lookup time v6  75,520.47ns
        Total lookup results v3 28583      -1152 differences
        Total lookup results v6 29735
    
