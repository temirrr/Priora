import GA
import greedy
import sys
import argparse
def normalised_fitness(max,min,point, weights): 
    (f1,f2,f3)=point.get_fitness()
    f=[f1,f2, f3]
    res=0
    for i in range(3):
        if max[i] != min[i]:
            res+=weights[i]*(f[i]-min[i])/(max[i]-min[i])
    return res

def select_from_front(front, w1, w2, w3):
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
    best=normalised_fitness(max,min,front[0], [w1, w2, w3])
    for i in range(1,len(front)):
        f=normalised_fitness(max,min,front[i], [w1, w2, w3])
        if f<best:
            index=i
            best=f
            
    return front[index]

def main(argv):
    path = argv[0]
    algo = argv[1]
    w1, w2, w3 = float(argv[2]), float(argv[3]), float(argv[4]) 
    tests_info = {}
    tests_name ={}
    tests = []
    k = 0
    file = path + 'tests_info.txt'
    with open(file, 'r') as f:
        while True:
            buf = f.readline()
            if not buf:
                break
            line = buf.split()
            each_name = ""
            i = 0
            while i + 3 < len(line):
                each_name += line[i]
                if i + 4 < len(line):
                    each_name += ' '
                i += 1
            each = list(map(lambda x: int(x), line[i:]))
            if each[2] == 0:
                each[0] = 0
            else:
                each[0] = each[0]/each[2]
            tests_name[k] = each_name
            tests_info[k] = each
            tests.append(k)
            k += 1
            print(k)
    fout = path + 'tests_ordering.txt'
    if k == 0:
        f = open(fout, 'w')
        f.close()
        return 
    if k == 1:
        f = open(fout, 'w')
        f.write(str(tests_name[0]) + '\n')
        f.close()
        return
    if k > 2 and algo=='NSGA':
        genalg = GA.GeneticAlgorithm(200,10,100,0.3, tests, tests_info)
        front, history = genalg.generate_solution()
        ret = select_from_front(front, w1, w2, w3)
        with open(fout, 'w') as f:
            for i in range (len(ret.order)):
                test_name = tests_name[ret.order[i]]
                #row = "{} {}".format(test_name,i)
                f.write(str(test_name) + '\n')
            f.close()
        return ret
    elif algo=="Greedy" or k < 3:
        order = greedy.sort(tests_info, w1, w2, w3)
        with open(fout, 'w') as f:
            for i in range (len(order)):
                test_name = tests_name[order[i][0]]
                #row = "{} {}".format(test_name,i)
                f.write(str(test_name) + '\n')
            f.close()
        return
    # #algo, w1, w2, w3 = parse_arguments()
    # algo = argv[0]
    # w1, w2, w3 = float(argv[1]), float(argv[2]), float(argv[3])
    # tests_info = {}
    # tests_name ={}
    # tests = []
    # k = 0
    # #file = 'tests_info.txt'
    # for buf in argv[4:]:
    #     each_name = buf.split()[0]
    #     each = list(map(lambda x: int(x), buf.split()[1:]))
    #     if each[2] == 0:
    #         each[0] = 0
    #     else:
    #         each[0] = each[0]/each[2]
    #     tests_name[k] = each_name
    #     tests_info[k] = each
    #     tests.append(k)
    #     k += 1
    # if k == 0:
    #     return
    # if algo=='NSGA' and k > 2:
    #     genalg = GA.GeneticAlgorithm(200,10,300,0.3, tests, tests_info)
    #     front, history = genalg.generate_solution()
    #     ret = select_from_front(front, w1, w2, w3)
    #     #fout = 'tests_ordering_nsga.txt'
    #     #with open(sys.stdout, 'w') as f:
    #     for i in range (len(ret.order)):
    #         test_name = tests_name[ret.order[i]]
    #         #row = "{} {}".format(test_name,i)
    #         #f.write(str(row) + '\n')
    #         print(test_name)
    #     #    f.close()
    #     return ret
    # elif k < 3 or algo=="Greedy":
    #     order = greedy.sort(tests_info, w1, w2, w3)
    #     #fout = 'tests_ordering_greedy.txt'
    #     #with open(sys.stdout, 'w') as f:
    #     for i in range (len(order)):
    #         test_name = tests_name[order[i][0]]
    #         #row = "{} {}".format(test_name,i)
    #         #f.write(str(row) + '\n')
    #         print(test_name)
    #     #    f.close()
    #     return

# def parse_arguments():
#     parser = argparse.ArgumentParser()
#     parser.add_argument('-a', type=str, default=50, help="algorithm")
#     parser.add_argument('-w1', type=float, default=29, help="weight for 1 objective")
#     parser.add_argument('-w2', type=float, default=0.3, help="weight for 2 objective")
#     parser.add_argument('-w3', type=float, default=False, help="weight for 3 objective")
#     args = parser.parse_args()
#     return args.a, args.w1, args.w2, args.w3
if __name__ == "__main__":
    main(sys.argv[1:])

    