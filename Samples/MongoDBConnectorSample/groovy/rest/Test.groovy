package rest


def bean = new Bean();
def ABean a = bean as ABean;
println a;

class Bean {

}

class ABean extends Bean{

}