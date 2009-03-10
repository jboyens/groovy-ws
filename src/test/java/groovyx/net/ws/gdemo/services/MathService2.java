package groovyx.net.ws.gdemo.services;

public class MathService2 {

    String toto;

    MathService2(String toto){
        this.toto = toto;
    }

    public double add(double arg0, double arg1){
        return arg0 + arg1;
    }
    public double square(double arg0){
        return arg0 * arg0;
    }

    public static void main(String[] args) {
       MathService2 ms = new MathService2("test");
       System.out.println(ms.add(1.0, 2.0));
    }
}

