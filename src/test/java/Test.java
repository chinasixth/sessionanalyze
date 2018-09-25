import java.util.Random;

/**
 * @ Author ：liuhao
 * @ Date   ：Created in 17:08 2018/9/25
 * @
 */
public class Test {
    public static void main(String[] args) {
        Random rand = new Random();
        for(int i = 0; i < 100; i++){
            System.out.println(rand.nextInt(100));
        }
    }
}
