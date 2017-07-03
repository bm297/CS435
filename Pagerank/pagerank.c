#include <iostream>
#include <list>
#include <fstream>
#include <vector>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>

#define iterBased 0
#define errorBased 1

using namespace std;

class Graph
{
	int V;
	vector<int> *aList;

public:
	Graph() {}

	Graph(int v)
	{
		V = v;
		aList = new vector<int>[v];
	}

	vector<int>* getList() {
		return aList;
	}

	void addEdge(int src, int dest)
	{
		aList[src].push_back(dest);
	}

	void printGraph()
	{
		for (int i = 0; i < V ; i++)
		{
			cout << "| " << i << " |-> ";
			for (int j = 0; j < aList[i].size(); j++)
			{
				cout << aList[i].at(j) << "->";
			}

			cout << endl;
		}
	}
};

double d = 0.85, error, initialValue;
int t, N, iter, iterations, iVal, method;
double P[100];
double oldP[100];
double tmp[100];
vector<int> outDegrees;

int convergence(){
	int i, j, k = 0, flag = 1;
	double err;

	if (method == iterBased && iterations > 0)
	{
		iterations--;
		flag = 0;
	}
	else if (method == errorBased)
	{
		for (int i = 0; i < N; i++)
		{
			if (fabs(P[i] - oldP[i]) > error) {
				return 0;
			}
		}
	}

	return flag;
}

void printOneVal() {
	for (int i = 0; i < N; i++)
	{
		printf("P[%d]: %.6f\n", i, P[i]);
	}
}

void printVals() {
	for (int i = 0; i < N; i++)
	{
		printf("P[%d]: %.6f   ", i, P[i]);
	}	
	printf("\n");
}

void init(Graph g) {

	for (int i = 0; i < N; i++) {
		P[i] = initialValue;
	}

	vector<int> *tp;
	tp = g.getList();
	int outDegree = 0;
	for (int i = 0; i < N; i++)
	{
		list<int>::iterator it;
		for (int j = 0; j < tp[i].size(); j++)
		{
			outDegree++;
		}
		outDegrees.push_back(outDegree);
		outDegree = 0;
	}

}

double C(Graph g, int k) {
	double sum = 0;
	vector<int> *tp = g.getList();

	for (int i = 0; i < N; i++)
	{
		for (int j = 0; j < tp[i].size(); j++)
		{
			if (tp[i].at(j) == k) {
				sum += P[i] / outDegrees[i];
			}
		}
	}

	return sum;
}

void pagerank(Graph g) {
	if (N<10)
	{
		cout << "Base\t:   " << iter << ":   ";
		printVals();
	}
	
	iter++;

	vector<double> tmps;
	double val, points;

	while (!convergence())
	{

		for (int i = 0; i < N; i++)
		{
			points = C(g, i);
			val = ((1 - d) / N) + d * (points);
			tmp[i] = val;
			val = 0;
		}

		for (int i = 0; i < N; i++)
		{
			oldP[i] = P[i];
			P[i] = tmp[i];
		}

		if (N<10)
		{
			cout << "iter\t:   " << iter << ":   ";
			printVals();
		}

		iter++;
	}	
		
	if (N>10)	
	{
		cout << "iter\t:   " << iter << endl;
		printOneVal();
	}
	

}

int main(int argc, char* argv[])
{
	method = iterBased;
	iterations = atoi(argv[1]);
	if (iterations == 0) {
		method = errorBased;
		error = pow(10, -4);
	}
	else if (iterations < 0) {
		method = errorBased;
		error = pow(10, -iterations);
	}
	else {
		iterations = iterations;
	}

	initialValue = atof(argv[2]);
	char* fn = argv[3];

	fstream ifs;
	int numVert, numEdge;
	int src, dest;

	ifs.open(fn, ios_base::in);
	ifs >> numVert >> numEdge;
	N = numVert;

	if (initialValue == -2) {
		initialValue = (double)1 / (double)sqrt(N);
	}
	else if (initialValue == -1) {
		initialValue = (double)1 / (double)N;
	}
	else if (initialValue == 0) {
		initialValue = 0;
	}
	else if (initialValue == 1) {
		initialValue = 1;
	}
	
	if (N > 10) {
		iter = 0;
		initialValue = -1;
		error = pow(10, -iterations);
		method = errorBased;
	}
	Graph g(numVert);

	while (ifs >> src >> dest)
	{
		g.addEdge(src, dest);
	}

	ifs.close();

	init(g);
	pagerank(g);

	return 0;
}