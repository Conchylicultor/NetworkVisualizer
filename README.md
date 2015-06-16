# NetworkVisualizer
Visualize the sequence network and allow edition to easily name the sequences.

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
1 3 1
2 3 0.8
1 4 0.8
2 4 0.64
3 4 0
1 5 0.4
2 5 0.96
```

The format used is the Pajek NET Format with the use of a label for the vertices and a weight for the edges. The labels contain the **sequence id** which is used to extract the informations, like images, about the sequences by looking on the _Traces_ folder (where the sequences informations are stored). An optional label parameters representing a **timestamp date** (for instance "date:1434421798") can be used to filter the vertices by time. The weight correspond to the **similarity score** (normalized between 0 and 1) and is used to visualize the recognition for differents threshold values.

After editions, two files are saved:
 * *traces_labelized.txt* contains the traces with the labelized sequences which could be used by the main program to either re-test, evaluate or train the reidentification algorithm.
 * *network_save.net* contains the edited graph which can be read be the sequence visualizer program. The saved graph after edition can look like this:

```
*Vertices 6
1 " seq:0_4 pers:Risa "
2 " seq:0_6 pers:Risa "
3 " seq:0_7 pers:Kaito "
4 " seq:0_9 pers:Risa "
5 " seq:0_12 pers:Uchiyama "
6 " seq:0_13 pers:Uchiyama "
*Edges
1 4 1
1 3 1
4 3 1
6 5 1
```

The correct paths for the file locations have to be defined manually.
