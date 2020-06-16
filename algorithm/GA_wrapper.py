import argparse
import math
import random
import operator
import sys
import copy
import numpy as np
import logging
from pymoo.model.problem import Problem
from pymoo.model.sampling import Sampling
from pymoo.model.crossover import Crossover
from pymoo.model.mutation import Mutation
from pymoo.algorithms.nsga3 import NSGA3
from pymoo.optimize import minimize
from pymoo.factory import get_reference_directions
from pymoo.factory import get_selection

# Wrapper classes for NSGA

class PrioProblem(Problem):

    def __init__(self, n, prio_problem):
        super().__init__(n_var=1, n_obj=n, n_constr=0, elementwise_evaluation=True)
        self.prio_problem = prio_problem

    def _evaluate(self, x, out, *args, **kwargs):
        out["F"] = np.array(x[0].get_fitness(), dtype=np.float)

class PrioSampling(Sampling):

    def _do(self, problem, n_samples, **kwargs):
        X = problem.prio_problem.create_initial_population()
        X = np.reshape(X, (problem.prio_problem.population_size,1))
        return X

class PrioCrossover(Crossover):

    def __init__(self):
        super().__init__(2,1)

    def _do(self, problem, X, **kwargs):
        _, n_matings, n_var = X.shape
        Y = np.full((1, n_matings, n_var), None, dtype=np.object)
        for k in range(n_matings):
            a, b = X[0, k, 0], X[1, k, 0]
            Y[0, k, 0] = problem.prio_problem.crossover(a,b)
        return Y


class PrioMutation(Mutation):

    def __init__(self):
        super().__init__()

    def _do(self, problem, X, **kwargs):
        for i in range(len(X)):
            X[i, 0] = problem.prio_problem.mutate(X[i, 0])
        return X




