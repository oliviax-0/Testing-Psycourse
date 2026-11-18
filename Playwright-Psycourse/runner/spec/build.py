import sys, os, json, importlib.util
here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, here)
tcs = []
for f in sorted(os.listdir(here)):
    if f.startswith("s") and f.endswith(".py") and f[1].isdigit():
        spec = importlib.util.spec_from_file_location(f[:-3], os.path.join(here, f)); m = importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
        tcs += m.TCS
json.dump(tcs, open(os.path.join(here, "tcs.json"), "w"), ensure_ascii=False, indent=1)
print(len(tcs), "TC")
