# test749 : LineCount2
import os
import matplotlib.pyplot as plt

def getpath(path):
    for i in [path + x for x in os.listdir(path)]:
        if os.path.isdir(i):
            if i[-1] != "/":
                i = i + "/"
            getpath(i)
        else:
            filenames.append(i)

def getline(path):
    enc, dlen = ["utf-8", "utf-16", "cp949"], 0
    for i in enc:
        try:
            with open(path, "r", encoding=i) as f:
                dlen = len( f.readlines() )
            return dlen
        except:
            pass
    return dlen

def draw(d, title):
    # 균등 분배
    lbl_n, sz_n, lbl, sz, drawf = [ ], [ ], [ ], [ ], lambda x: f"{x:.1f}%\n({round(sum(sz)*x/100):d})"
    for i in d:
        if d[i] != 0:
            lbl.append(i)
            sz.append(d[i])
    sz, lbl = zip( *sorted( list( zip(sz, lbl) ) ) )
    l, r = 0, len(sz) - 1
    while l <= r:
        if l == r:
            lbl_n.append( lbl[l] )
            sz_n.append( sz[l] )
        else:
            lbl_n.append( lbl[r] )
            sz_n.append( sz[r] )
            lbl_n.append( lbl[l] )
            sz_n.append( sz[l] )
        l, r = l + 1, r - 1
    print(f"전체 라인수 : {sum(sz)}")

    # 파이 차트 그리기
    wedges, texts, autotexts = plt.pie(sz_n, labels=lbl_n, autopct=drawf, shadow=True, startangle=270, labeldistance=0.9, pctdistance=1.3)
    plt.legend( wedges, [f'{l}: {s}' for l, s in zip(lbl_n, sz_n)], loc='lower right', bbox_to_anchor=(1.1, -0.1) )
    plt.axis('equal')
    plt.title(title)
    plt.show()

# ========== 설정할 값들 ==========
path = "./" # 카운트 대상 폴더 경로
code_ext = ("c", "cpp", "cs", "css", "dart", "go", "h", "hpp", "java", "js", "kt", "lua", "php", "py", "r", "rb", "rs", "sh", "ts") # 텍스트 코드 확장자
data_ext = ("csv", "htm", "html", "json", "txt", "md") # 텍스트 데이터 확장자
# ========== 설정 종료 ==========

# 기본 데이터 가져오기
path = os.path.abspath(path).replace("\\", "/")
if path[-1] != "/":
    path = path + "/"
filenames, code, data = [ ], dict(), dict()
for i in code_ext:
    code[i] = 0
for i in data_ext:
    data[i] = 0
getpath(path)
print("파일명 가져오기 완료")

# 라인수 데이터 가져오기
for i in filenames:
    ext = ""
    if "." in i:
        ext = i[i.rfind(".")+1:].lower()
    if ext in code:
        code[ext] = code[ext] + getline(i)
    elif ext in data:
        data[ext] = data[ext] + getline(i)
print("라인수 가져오기 완료")

# 그리기
draw(code, "Source Code LineCount")
draw(data, "Text Data LineCount")
