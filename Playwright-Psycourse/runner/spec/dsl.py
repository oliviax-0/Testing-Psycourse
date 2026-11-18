# DSL kecil untuk menulis TC. Satu "row" = satu baris langkah di spreadsheet.
def o(op, **kw):
    d = {"op": op}; d.update(kw); return d

def TC(id, story, title, *rows):
    steps = []
    for i, row in enumerate(rows, 1):
        for st in (row if isinstance(row, list) else [row]):
            st = dict(st); st["r"] = i; steps.append(st)
    return {"id": id, "story": story, "title": title, "steps": steps}

SUBMIT = "form button.form-submit"
def submit(): return o("clickCss", css=SUBMIT)
def see(*texts): return [o("see", text=t) for t in texts]
def at(path, exact=False): return o("url", path=path, exact=exact)
def stays(path): return o("urlStays", path=path)
def fill(**kw): return [o("fill", name=k, value=v) for k, v in kw.items()]
def reg(name="Olivia Tester", email="{EMAIL}", pw="Rahasia123", cf=None, consent=True):
    ops = [o("open", path="/register")]
    ops += fill(name=name, email=email, password=pw, confirmPassword=pw if cf is None else cf)
    if consent: ops.append(o("check", name="privacyConsent"))
    return ops
def login_try(email, pw):
    return [o("open", path="/login")] + fill(email=email, password=pw) + [submit()]
RESET = o("resetRate")
