import GA
import greedy
import sys
def normalised_fitness(max,min,point): 
    (f1,f2,f3)=point.get_fitness()
    f=[f1,f2, f3]
    res=0
    for i in range(3):
        if max[i] != min[i]:
            res+=0.2*(f[i]-min[i])/(max[i]-min[i])
    return res

def select_from_front(front):
    (f1,f2, f3)=front[0].get_fitness()
    max=[f1,f2, f3]
    min=[f1,f2, f3]
    for i in range(len(front)):
        (f1,f2, f3)=front[i].get_fitness()
        f=[f1,f2, f3]
        for obj in range(3):
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

def main(argv):
    tests_info = {}
    tests_name ={}
    tests = []
    k = 0
    with open(file, 'r') as f:
        while True:
            buf = f.readline()
            if not buf:
                break
            each_name = buf.split()[0]
            each = list(map(lambda x: int(x), buf.split()[1:]))
            tests_name[k] = each_name
            tests_info[k] = each
            tests.append(k)
            k += 1
    genalg = GA.GeneticAlgorithm(200,10,300,0.3, tests, tests_info)
    front, history = genalg.generate_solution()
    ret = select_from_front(front)
    print(ret)

    fout = 'tests_ordering.txt'
    with open(fout, 'w') as f:
        for i in range (len(ret.order)):
            test_name = tests_name[ret.order[i]]
            row = "{} {}".format(test_name,i)
            f.write(str(row) + '\n')
        f.close()
    return ret
if __name__ == "__main__":
    main(sys.argv[1:])