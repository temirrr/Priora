class Chromosome():
    def __init__(self, order, tests_info):
        self.order = order
        self.tests_info = tests_info
        self.fitness = self.get_fitness()
        self.infeasability = None
    def __str__(self):
        return f"Chromosome: {self.order}"
    def __repr__(self):
        return f"Chromosome: {self.order}"

    #maximazi both objectives, in return they are insersed to minimize
    def get_fitness(self):
        success_v = 0
        time_v = 0
        counter = 0
        n = len(self.order)
        for test in self.order:
            counter += 1
            if(self.tests_info[test][0]):
                success_v += 1*counter
            time_v += self.tests_info[test][1] * counter
        return (success_v, time_v)
    
    def get_infeasability(self):
        if self.infeasability is not None:
            return self.infeasability
        infeasability_score = 0
        for test in self.order:
            if (self.tests_info[test][0]):
                break
            else:
                infeasability_score += 1
        self.infeasability = infeasability_score
        return infeasability_score




