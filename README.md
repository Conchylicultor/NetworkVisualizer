# NetworkVisualizer
Visualize the sequence network and allow edition to easily name the sequences

Example of imput file:

```
*Vertices 6
1 " seq:0_4 "
2 " seq:0_6 "
3 " seq:0_7 "
4 " seq:0_9 "
5 " seq:0_12 "
6 " seq:0_13 "
*Edges
1 2 0.8
1 3 2
2 3 0.8
1 4 0.8
2 4 0.64
3 4 1.76
1 5 0.4
2 5 0.96
```

The format use is the Pajek NET Format with the use of a label for the vertices and a weight ( between 0 and 2) for the edges. The saved graph after edition can look like this:

```
*Vertices 6
1 " seq:0_4 pers:Risa "
2 " seq:0_6 pers:Risa "
3 " seq:0_7 pers:Kaito "
4 " seq:0_9 pers:Risa "
5 " seq:0_12 pers:Uchiyama "
6 " seq:0_13 pers:Uchiyama "
*Edges
1 4 2
1 3 2
4 3 2
6 5 2
```
