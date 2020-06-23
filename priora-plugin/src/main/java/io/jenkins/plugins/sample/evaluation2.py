import sys

def lineprint(tests):
    for i in tests:
        print(i)

def main(argv):
    tests_info = {}
    tests = []
    method = argv[0]
    w_fails = float(argv[1])
    w_duration = float(argv[2])
    w_runs = float(argv[3])
    for line in argv[4:]:
        split_line = line.split()
        tests_info[split_line[0]] = list(map(lambda x: int(x), split_line[1:]))
        tests.append(split_line[0])
    if (method == "NSGA"):
        lineprint(tests)
    if (method == "Greedy"):
        lineprint(tests)
    if (method == "Random"):
        lineprint(tests)

if __name__ == "__main__":
    main(sys.argv[1:])
