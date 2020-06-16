def sort(tests_data):
    first = []
    second = []
    for k, v in tests_data.items():
        if v[0]==0:
            first.append(k)
        else:
            second.append(k)
    first = [k for k in sorted(first, key=lambda item: tests_data[item][1])]
    second = [k for k in sorted(second, key=lambda item: tests_data[item][1])]
    return first+second