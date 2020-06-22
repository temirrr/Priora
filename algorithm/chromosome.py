class Chromosome():
    def __init__(self, order,tests_info):
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
        off1 = self.off1()
        off2 = self.off2()
        off3 = self.off3()
        return (off1, off2, off3)

    def off1(self):
        fails = self.tests_info[self.order[0]][0]
        count = 1
        for i in range(len(self.order)):
            if i+1< len(self.order) and self.tests_info[self.order[i]][0]<= self.tests_info[self.order[i+1]][0]:
                fails += self.tests_info[self.order[i+1]][0]
                count +=1
            else:
                break
        return fails/count

    def off2(self):
        time_v = self.tests_info[self.order[0]][1]
        count = 1
        for i in range(len(self.order)):
            if i+1< len(self.order) and self.tests_info[self.order[i]][1]<= self.tests_info[self.order[i+1]][1]:
                time_v += self.tests_info[self.order[i+1]][1]
                count +=1
            else:
                break
        return time_v/count
    def off3(self):
        number = self.tests_info[self.order[0]][2]
        count = 1
        for i in range(len(self.order)):
            if i+1< len(self.order) and self.tests_info[self.order[i]][2]<= self.tests_info[self.order[i+1]][2]:
                number += self.tests_info[self.order[i+1]][2]
                count +=1
            else:
                break
        return number/count

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




