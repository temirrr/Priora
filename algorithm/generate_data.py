import random
file = 'tests_info.txt'
with open(file, 'w') as f:
    for i in range (0,100):
        time = random.randint(1, 10000)
        number = random.randint(0,10)
        fails = random.randint(0, number)
        row =  "{} {} {} {}".format("T"+str(i), fails, time, number)
        f.write(str(row) + '\n')
    f.close()