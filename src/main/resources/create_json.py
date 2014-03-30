import json

company_list = "3M, American Express, amex, , AT&T, Boeing, Caterpillar, Chevron, Cisco, Coca Cola, DuPont, Exxon, General Electric, Goldman Sachs, Home Depot, intel, ibm, johnson johnson, jpmorgan, mcdonalds, merck, microsoft, nike, pfizer, procter, travelers, united health group, united technologies, verizon, visa, wal mart"

output = {}

for s in company_list.split(','):
    s = s.strip()

    if s == "":
        continue

    output[s] = [s]

output["Disney"] = ["walt disney", "Disney"]

with open("company_map.json", 'w') as f:
    f.write(json.dumps(output))
