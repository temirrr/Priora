import GA
import greedy
def normalised_fitness(max,min,point): 
    (f1,f2)=point.get_fitness()
    f=[f1,f2]
    res=0
    for i in range(2):
        if max[i] != min[i]:
            res+=0.2*(f[i]-min[i])/(max[i]-min[i])
    return res

def select_from_front(front):
    (f1,f2)=front[0].get_fitness()
    max=[f1,f2]
    min=[f1,f2]
    for i in range(len(front)):
        (f1,f2)=front[i].get_fitness()
        f=[f1,f2]
        for obj in range(2):
            if f[obj]>max[obj]:
                max[obj]=f[obj]  
            if f[obj]<min[obj]:
                min[obj]=f[obj]
    index=0
    best=normalised_fitness(max,min,front[0])
    for i in range(1,len(front)):
        f=normalised_fitness(max,min,front[i])
        if f<best:
            index=i
            best=f
            
    return front[index]

def main():
    file = 'tests_info.txt'
    tests_info = {}
    tests = []
    with open(file, 'r') as f:
        while True:
            buf = f.readline()
            if not buf:
                break
            each = list(map(lambda x: int(x), buf.split()))
            tests_info[each[0]] = each[1:]
            tests.append(each[0])
    genalg = GA.GeneticAlgorithm(200,10,300,0.3, tests, tests_info)
    front, history = genalg.generate_solution()
    ret = select_from_front(front)
    print(ret)
    print(greedy.sort(tests_info))
    return ret
if __name__ == "__main__":
    main()