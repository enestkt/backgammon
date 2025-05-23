//package ui;
//
//import javax.swing.*;
//import java.awt.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//
//public class AnimationPanel extends JPanel {
//
//    private int currentX = 50, currentY = 50;
//    private int targetX = 300, targetY = 300;
//    private boolean animating = false;
//    private Runnable onFinish;
//
//    public AnimationPanel(Runnable onFinish) {
//        this.onFinish = onFinish;
//        setBackground(Color.WHITE);
//        startAnimation(300, 300);
//    }
//
//    private void startAnimation(int toX, int toY) {
//        if (animating) return;
//
//        targetX = toX;
//        targetY = toY;
//        animating = true;
//
//        Timer timer = new Timer(10, null);
//        timer.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (Math.abs(currentX - targetX) <= 2 && Math.abs(currentY - targetY) <= 2) {
//                    currentX = targetX;
//                    currentY = targetY;
//                    animating = false;
//                    ((Timer) e.getSource()).stop();
//
//                    if (onFinish != null) {
//                        onFinish.run(); // animasyon bittiğinde yapılacak işlem
//                    }
//                } else {
//                    currentX += Math.signum(targetX - currentX) * 4;
//                    currentY += Math.signum(targetY - currentY) * 4;
//                    repaint();
//                }
//            }
//        });
//        timer.start();
//    }
//
//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        g.setColor(Color.BLACK);
//        g.fillOval(currentX, currentY, 40, 40);
//    }
//}