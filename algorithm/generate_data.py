import random
file = 'tests_info.txt'
with open(file, 'w') as f:
    for i in range (0,100):
        success = random.randint(0, 1)
        time = random.randint(1, 10000)
        row = "{} {} {}".format(i, success, time)
        f.write(str(row) + '\n')
    f.close()