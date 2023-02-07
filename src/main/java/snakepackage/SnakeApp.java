package snakepackage;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import enums.GridSize;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * @author jd-
 *
 */
public class SnakeApp {
    private JButton btn_iniciar, btn_pausar, btn_reanudar;
    private JLabel lbl_serpienteMasLarga, lbl_serpientePeor;
    private static SnakeApp app;
    public static final int MAX_THREADS = 8;
    Snake[] snakes = new Snake[MAX_THREADS];
    private static final Cell[] spawn = {
            new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
//        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
//        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
//        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
//        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
//        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
//        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2),
//        new Cell(1, (GridSize.GRID_HEIGHT / 2) / 2)};
            new Cell(GridSize.GRID_WIDTH - 2,
                    3 * (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2, 1),
            new Cell((GridSize.GRID_WIDTH / 2) / 2, GridSize.GRID_HEIGHT - 2),
            new Cell(1, 3 * (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell(GridSize.GRID_WIDTH - 2, (GridSize.GRID_HEIGHT / 2) / 2),
            new Cell((GridSize.GRID_WIDTH / 2) / 2, 1),
            new Cell(3 * (GridSize.GRID_WIDTH / 2) / 2,
                    GridSize.GRID_HEIGHT - 2)};
    private JFrame frame;
    private static Board board;
    int nr_selected = 0;
    Thread[] thread = new Thread[MAX_THREADS];

    public SnakeApp() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame = new JFrame("The Snake Race");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(618, 640);
        frame.setSize(GridSize.GRID_WIDTH * GridSize.WIDTH_BOX + 17,
                GridSize.GRID_HEIGHT * GridSize.HEIGH_BOX + 40);
        frame.setLocation(dimension.width / 2 - frame.getWidth() / 2,
                dimension.height / 2 - frame.getHeight() / 2);
        board = new Board();


        frame.add(board,BorderLayout.CENTER);

        JPanel actionsBPabel = new JPanel();
        actionsBPabel.setLayout(new FlowLayout());
        btn_iniciar = new JButton("Iniciar");
        btn_pausar = new JButton("Pausar");
        btn_reanudar = new JButton("Reanudar");
        actionsBPabel.add(btn_iniciar);
        actionsBPabel.add(btn_pausar);
        actionsBPabel.add(btn_reanudar);
        frame.add(actionsBPabel,BorderLayout.SOUTH);


        JPanel actionsInformationPanel = new JPanel();
        actionsInformationPanel.setLayout(new FlowLayout());
        lbl_serpienteMasLarga = new JLabel("Serpiente mas larga:");
        actionsInformationPanel.add(lbl_serpienteMasLarga);
        lbl_serpientePeor = new JLabel("Peor serpiente:");
        actionsInformationPanel.add(lbl_serpientePeor);
        frame.add(actionsInformationPanel,BorderLayout.NORTH);

        definaAccionesBotones();
        estadoInicialBotones();
    }

    /**
     * Metodo encargado de dar la acci�n a los botones.
     */
    public void definaAccionesBotones(){
        btn_iniciar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                accionBotonIniciar();
            }
        });
        btn_pausar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                accionBotonPausar();
            }
        });
        btn_reanudar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                accionBotonReanudar();
            }
        });
    }

    /**
     * Metodo encargado de dajar en su estado inicial los botones, donde se desactivan tanto pausar como reanudar
     */
    public void estadoInicialBotones(){
        btn_pausar.setEnabled(false);
        btn_reanudar.setEnabled(false);
    }

    /**
     * Metodo encargado de darl la accion al boton iniciar donde se desactiva el boton iniciar se activa el boton pausar y se inician los hilos
     */
    public void accionBotonIniciar(){
        btn_iniciar.setEnabled(false);
        btn_pausar.setEnabled(true);
        for(int i=0; i<thread.length; i++){
            thread[i].start();
        }
    }
    /**
     * Metodo encargado de darl la accion al boton reanudar donde se desactiva el boton reanudar se activa el boton pausar y se despiertan los hilos
     */
    public void accionBotonReanudar(){
        btn_reanudar.setEnabled(false);
        btn_pausar.setEnabled(true);

        for(int i = 0;i < snakes.length;i++ ) {
            snakes[i].despertar();
        }
        synchronized(getApp()){
            getApp().notifyAll();
        }
    }

    /**
     * Metodo encargado de darle la accion al boton pausar donde se desactiva el boton pausar se activa el boton reanudar y se reactivan los hilos
     */
    public void accionBotonPausar(){
        btn_pausar.setEnabled(false);
        btn_reanudar.setEnabled(true);
        for(int i = 0;i < snakes.length ;i++ ) {
            snakes[i].dormir();
        }
        int serpientePeor = traerLaSerpientePeor();
        int serpienteMasLarga = traerSerpienteVivaMasLarga();
        if(serpientePeor==-1){
            lbl_serpientePeor.setText("No se ha muerto ninguna serpiente.");
        }
        else{
            lbl_serpientePeor.setText("La peor serpiente es: "+serpientePeor+".");
        }

        if(serpienteMasLarga==-1){
            lbl_serpienteMasLarga.setText("No hay ninguna serpiente viva");
            btn_pausar.setEnabled(false);
            btn_iniciar.setEnabled(false);
            btn_reanudar.setEnabled(false);
            JOptionPane.showMessageDialog(null, "Ya no hay mas serpientes vivas, el juego ha terminado", "Juego Terminado", JOptionPane.INFORMATION_MESSAGE);
        }
        else{
            lbl_serpienteMasLarga.setText("La serpiente viva mas larga es: "+serpienteMasLarga+".");
        }

    }
    public static void main(String[] args) {
        app = new SnakeApp();
        app.init();
    }

    private void init() {



        for (int i = 0; i != MAX_THREADS; i++) {

            snakes[i] = new Snake(i + 1, spawn[i], 1);
            snakes[i].addObserver(board);
            thread[i] = new Thread(snakes[i]);
//            thread[i].start();
        }

        frame.setVisible(true);


        for(int i=0; i<snakes.length; i++){
            try {
                thread[i].join();
            } catch (InterruptedException ex) {
                Logger.getLogger(SnakeApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        System.out.println("Thread (snake) status:");
        for (int i = 0; i != MAX_THREADS; i++) {
            System.out.println("["+i+"] :"+thread[i].getState());
        }
    }

    public static SnakeApp getApp() {
        return app;
    }

    /**
     * Metodo encargado de traer el Id de la serpiente
     * @return int <br> Si no hay ninguna serpiente muerta retorna <b>-1</b>
     */
    private int traerLaSerpientePeor(){
        int serpientePeor = -1;
        int ordenMuertePeor = 999999;
        //Validar que las serpientes se hayan detenido
        int i=0;
        while(i<snakes.length){
//            System.out.println("snakepackage.SnakeApp.traerLaSerpientePeor()");
            //Si la serpiente ya se murio o esta esperando
            if(thread[i].getState()== Thread.State.WAITING || thread[i].getState()== Thread.State.TERMINATED){
                if(snakes[i].isSnakeEnd()){
                    //Si el orden de muerte que viene de la serpiente en la posicion i es menor al que ya se ha encontrado,
                    //entonces la serpiente en la posicion i murio primero
                    if(ordenMuertePeor>=snakes[i].getOrdenMuerte()){
                        serpientePeor = snakes[i].getIdt();
                        ordenMuertePeor = snakes[i].getOrdenMuerte();
                    }
                }
                i++;

            }
        }
        return serpientePeor;
    }

    /**
     * Metodo encargado de traer la serpiente viva m�s larga
     * @return int <br> Si no hay ninguna serpiente viva retorna <b>-1</b>
     */
    private int traerSerpienteVivaMasLarga(){
        int idSerpienteVivaMasLarga = -1;
        int tamanoSerpienteVivaMasLarga = -1;
        for(int i=0; i<snakes.length; i++){
            //Si la serpiente no se ha muerto
            if(!snakes[i].isSnakeEnd()){
                //Si la longitud de la serpiente en la posicion i es mayor o igual a la actual mas larga,
                //entonces la serpiente en la posicion i es mayor
                if(tamanoSerpienteVivaMasLarga<=snakes[i].longitudSerpiente()){
                    idSerpienteVivaMasLarga = snakes[i].getIdt();
                    tamanoSerpienteVivaMasLarga = snakes[i].longitudSerpiente();
                }
            }
        }

        return idSerpienteVivaMasLarga;
    }

}