def sort(tests_data, w1, w2, w3):
    max_1 = sorted([v[0] for k, v in tests_data.items()])[len(tests_data)-1]
    min_1 = sorted([v[0] for k, v in tests_data.items()])[0]
    max_2 = sorted([v[1] for k, v in tests_data.items()])[len(tests_data)-1]
    min_2 = sorted([v[2] for k, v in tests_data.items()])[0]
    max_3 = sorted([v[2] for k, v in tests_data.items()])[len(tests_data)-1]
    min_3 = sorted([v[2] for k, v in tests_data.items()])[0]
    order = []
    for i in range(len(tests_data)):
        res = 0
        if max_1 != min_1:
            res += w1 * (tests_data[i][0]-min_1)/(max_1-min_1)
        if max_2 != min_2:
            res += w2 * (tests_data[i][1]-min_2)/(max_2-min_2)
        if max_3 != min_3:
            res += w3 * (tests_data[i][2]-min_3)/(max_3-min_3)
        order.append((i, res))
    order.sort(key=lambda tup: tup[1])
    return order