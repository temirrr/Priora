import random
file = 'tests_info.txt'
with open(file, 'w') as f:
    for i in range (0,100):
        success = random.randint(0, 1)
        time = random.randint(1, 10000)
        number = random.randint(0,10)
        row = "{} {} {} {}".format("T"+str(i), success, time, number)
        f.write(str(row) + '\n')
    f.close()