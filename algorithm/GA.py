import chromosome as ch
import random
import numpy as np
from pymoo.model.problem import Problem
from pymoo.model.sampling import Sampling
from pymoo.model.crossover import Crossover
from pymoo.model.mutation import Mutation
from pymoo.algorithms.nsga3 import NSGA3
from pymoo.algorithms.nsga2 import NSGA2
from pymoo.optimize import minimize
from pymoo.factory import get_reference_directions
from pymoo.factory import get_selection
import GA_wrapper
def feasability_tournament(pop, P, algorithm, **kwargs):
    # P is a matrix with chosen indices from pop
    n_tournaments, n_competitors = P.shape
    S = np.zeros(n_tournaments, dtype=np.int)
    for i in range(n_tournaments):
        tournament = P[i]
        scores = np.zeros(n_competitors, dtype=np.int)
        for j in range(n_competitors):
            infeasability_score = pop[tournament[j]].X[0].get_infeasability()
            scores[j] = infeasability_score
        winner_index = scores.argsort()[0]
        S[i] = tournament[winner_index]
    return S
def func_is_duplicate(pop, *other, **kwargs):
    if len(other) == 0:
        return np.full(len(pop), False)

    # value to finally return
    is_duplicate = np.full(len(pop), False)

    return is_duplicate
class GeneticAlgorithm():
    def __init__(self, population_size, tournament_size, max_generations, mutation_rate, tests, tests_info):
        self.population_size = population_size
        self.tournament_size = tournament_size
        self.max_generations = max_generations
        self.mutation_rate = mutation_rate
        self.tests = tests
        self.tests_info = tests_info
    
    def generate_chromosome(self):
        first = []
        second = []
        for test in self.tests:
            if (self.tests_info[test][0]):
                second.append(test)
            else:
                first.append(test)
        random.shuffle(first)
        random.shuffle(second)
        ret = first + second
        return ch.Chromosome(ret, self.tests_info)
    
    def create_initial_population(self):
        initial_population = []
        for _ in range(self.population_size):
            initial_population.append(self.generate_chromosome())
        return initial_population
    
    def crossover(self, p1, p2):
        i1, i2 = random.sample(range(len(p1.order) - 1), 2)
        child_order = [-1]*len(p1.order)
        if i1<i2:
            middle = p1.order[i1:i2]
            child_order[i1:i2] = middle
            non_middle = p1.order[:i1]+p1.order[i2:]
            p2_candidates = []
            for j in range(len(p2.order)):
                if p2.order[j] not in middle:
                    p2_candidates.append(p2.order[j])
            k = 0
            for j in range(0, i1):
                child_order[j] = p2_candidates[k]
                k+=1
            for j in range(i2, len(p2.order)):
                child_order[j] = p2_candidates[k]
                k+=1
            # k = 0
            # for j in range(len(p2.order)):
            #     if child_order[j] == -1:
            #         child_order[j] = non_middle[k]
            #         k+=1
                # else:
                #     print(k)
                #     child_order[j] = non_middle[k]
                #     k += 1
        else:
            middle = p1.order[i2:i1]
            child_order[i2:i1] = middle
            non_middle = p1.order[:i2]+p1.order[i1:]
            p2_candidates = []
            for j in range(len(p2.order)):
                if p2.order[j] not in middle:
                    p2_candidates.append(p2.order[j])
            k = 0
            for j in range(0, i2):
                child_order[j] = p2_candidates[k]
                k+=1
            for j in range(i1, len(p2.order)):
                child_order[j] = p2_candidates[k]
                k+=1
        # print("parent1", p1.order)
        # print("parent2", p2.order)
        # print("i1, i2", i1, i2)
        # print("Crossover child", child_order)
        # crossover_point = random.randint(1, len(p1.order) - 1)
        # rand_int = random.randint(0, 1)
        # if rand_int == 1:
        #     child_order = (p1.order[:crossover_point] +  #here could be problem with synchronization, as node should be passed by reference and not by instance
        #              p2.order[crossover_point:])
        # else:
        #     child_order = (p2.order[crossover_point:] +
        #              p1.order[:crossover_point])
        return ch.Chromosome(child_order, self.tests_info)
    
    def mutate(self,chromosome):
        return self.swap_mutation(chromosome)
    
    def swap_mutation(self, chromosome):
        if random.random() < self.mutation_rate:
            i1, i2 = random.sample(range(len(chromosome.order)), 2)
            tmp = chromosome.order[i1]
            chromosome.order[i1] = chromosome.order[i2]
            chromosome.order[i2] = tmp
            chromosome.fitness = chromosome.get_fitness()
        return chromosome
    

    def get_fittest(self, candidates):
        score_n_candidate_list = []
        for chromosome in candidates:
            infeasability_score = chromosome.get_infeasability()
            score_n_candidate_list.append((infeasability_score, chromosome))
        score_n_candidate_list.sort(key=lambda tup: tup[0])
        return score_n_candidate_list[len(candidates)-1][1]

    def selection(self, population):
        #Tournament selection   P/2 size might be better
        mating_pool = []
        while len(mating_pool) < len(population)//2: #or self.mat_pool_size
            participants = random.sample(population, self.tournament_size) #self.tournament_size
            fittest = self.get_fittest(participants)
            mating_pool.append(fittest)
        return mating_pool
    
    def generate_new_population(self, old_population):
        mating_pool = np.array(self.selection(old_population))
        new_population = []
        for i in range(len(old_population)):
            p1, p2 = np.random.choice(mating_pool, 2)
            new_solution = self.crossover(p1, p2)
            new_solution = self.mutate(new_solution)
            new_population.append(new_solution)
        return new_population

    def generate_solution(self):
        ref_dirs = get_reference_directions("das-dennis", 3, n_partitions=8)
        selection = get_selection('tournament',func_comp=feasability_tournament,pressure=self.tournament_size)
        algorithm = NSGA2(pop_size=self.population_size, sampling=GA_wrapper.PrioSampling(), selection = selection,
                            crossover=GA_wrapper.PrioCrossover(), mutation=GA_wrapper.PrioMutation(),
                            eliminate_duplicates=False)
        res = minimize(GA_wrapper.PrioProblem(3,  self),
                       algorithm,
                       seed=1,
                       verbose=True,
                       save_history=True,
                       termination=('n_gen', self.max_generations))
        return res.X.flatten(), res.history


        