/**
 * Created by zqh on 2017/5/21.
 */
 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 主界面
 *
 * @author yangle
 */
public class MainFrame extends JFrame {

    /**
     * 程序界面宽度
     */
    public static final int WIDTH = 500;

    /**
     * 程序界面高度
     */
    public static final int HEIGHT = 360;

    private JTextArea dataView = new JTextArea();
    private JScrollPane scrollDataView = new JScrollPane(dataView);

    // 串口设置面板
    private JPanel serialPortPanel = new JPanel();
    private JLabel serialPortLabel = new JLabel("串口");
    private JLabel baudrateLabel = new JLabel("波特率");
    private JComboBox commChoice = new JComboBox();
    private JComboBox baudrateChoice = new JComboBox();

    // 操作面板
    private JPanel operatePanel = new JPanel();
    private JTextField dataInput = new JTextField();
    private JButton serialPortOperate = new JButton("打开串口");
    private JButton sendData = new JButton("发送数据");

    private List<String> commList = null;
    private SerialPort serialport;

    public MainFrame() {
        initView();
        initComponents();
        actionListener();
        initData();
    }

    private void initView() {
        // 关闭程序
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // 禁止窗口最大化
        setResizable(false);

        // 设置程序窗口居中显示
        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getCenterPoint();
        setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, WIDTH, HEIGHT);
        this.setLayout(null);

        setTitle("串口通讯");
    }

    private void initComponents() {
        // 数据显示
        dataView.setFocusable(false);
        scrollDataView.setBounds(10, 10, 475, 200);
        add(scrollDataView);

        // 串口设置
        serialPortPanel.setBorder(BorderFactory.createTitledBorder("串口设置"));
        serialPortPanel.setBounds(10, 220, 170, 100);
        serialPortPanel.setLayout(null);
        add(serialPortPanel);

        serialPortLabel.setForeground(Color.gray);
        serialPortLabel.setBounds(10, 25, 40, 20);
        serialPortPanel.add(serialPortLabel);

        commChoice.setFocusable(false);
        commChoice.setBounds(60, 25, 100, 20);
        serialPortPanel.add(commChoice);

        baudrateLabel.setForeground(Color.gray);
        baudrateLabel.setBounds(10, 60, 40, 20);
        serialPortPanel.add(baudrateLabel);

        baudrateChoice.setFocusable(false);
        baudrateChoice.setBounds(60, 60, 100, 20);
        serialPortPanel.add(baudrateChoice);

        // 操作
        operatePanel.setBorder(BorderFactory.createTitledBorder("操作"));
        operatePanel.setBounds(200, 220, 285, 100);
        operatePanel.setLayout(null);
        add(operatePanel);

        dataInput.setBounds(25, 25, 235, 20);
        operatePanel.add(dataInput);

        serialPortOperate.setFocusable(false);
        serialPortOperate.setBounds(45, 60, 90, 20);
        operatePanel.add(serialPortOperate);

        sendData.setFocusable(false);
        sendData.setBounds(155, 60, 90, 20);
        operatePanel.add(sendData);
    }

    @SuppressWarnings("unchecked")
    private void initData() {
        commList = SerialPortManager.findPort();
        // 检查是否有可用串口，有则加入选项中
        if (commList == null || commList.size() < 1) {
            ShowUtils.warningMessage("没有搜索到有效串口！");
        } else {
            for (String s : commList) {
                commChoice.addItem(s);
            }
        }

        baudrateChoice.addItem("9600");
        baudrateChoice.addItem("19200");
        baudrateChoice.addItem("38400");
        baudrateChoice.addItem("57600");
        baudrateChoice.addItem("115200");
    }

    private void actionListener() {
        serialPortOperate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ("打开串口".equals(serialPortOperate.getText())
                        && serialport == null) {
                    openSerialPort(e);
                } else {
                    closeSerialPort(e);
                }
            }
        });

        sendData.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sendData(e);
            }
        });
    }

    /**
     * 打开串口
     *
     * @param evt
     *            点击事件
     */
    private void openSerialPort(ActionEvent evt) {
        // 获取串口名称
        String commName = (String) commChoice.getSelectedItem();
        // 获取波特率
        int baudrate = 9600;
        String bps = (String) baudrateChoice.getSelectedItem();
        baudrate = Integer.parseInt(bps);

        // 检查串口名称是否获取正确
        if (commName == null || commName.equals("")) {
            ShowUtils.warningMessage("没有搜索到有效串口！");
        } else {
                serialport = SerialPortManager.openPort(commName, baudrate);
                if (serialport != null) {
                    dataView.setText("串口已打开" + "\r\n");
                    serialPortOperate.setText("关闭串口");
                }
        }
            SerialPortManager.addListener(serialport, new SerialListener());
    }

    /**
     * 关闭串口
     *
     * @param evt
     *            点击事件
     */
    private void closeSerialPort(ActionEvent evt) {
        SerialPortManager.closePort(serialport);
        dataView.setText("串口已关闭" + "\r\n");
        serialPortOperate.setText("打开串口");
    }

    /**
     * 发送数据
     *
     * @param evt
     *            点击事件
     */
    private void sendData(ActionEvent evt) {
        // 输入框直接输入十六进制字符，长度必须是偶数
        String data = dataInput.getText().toString();
            SerialPortManager.sendToPort(serialport,
                    ByteUtils.hexStr2Byte(data));
    }

    private class SerialListener implements SerialPortEventListener {
        /**
         * 处理监控到的串口事件
         */
        public void serialEvent(SerialPortEvent serialPortEvent) {

            switch (serialPortEvent.getEventType()) {

                case SerialPortEvent.BI: // 10 通讯中断
                    ShowUtils.errorMessage("与串口设备通讯中断");
                    break;

                case SerialPortEvent.OE: // 7 溢位（溢出）错误

                case SerialPortEvent.FE: // 9 帧错误

                case SerialPortEvent.PE: // 8 奇偶校验错误

                case SerialPortEvent.CD: // 6 载波检测

                case SerialPortEvent.CTS: // 3 清除待发送数据

                case SerialPortEvent.DSR: // 4 待发送数据准备好了

                case SerialPortEvent.RI: // 5 振铃指示

                case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
                    break;

                case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
                    byte[] data = null;
                    try {
                        if (serialport == null) {
                            ShowUtils.errorMessage("串口对象为空！监听失败！");
                        } else {
                            // 读取串口数据
                            data = SerialPortManager.readFromPort(serialport);
                            dataView.append(ByteUtils.byteArrayToHexString(data,
                                    true) + "\r\n");
                        }
                    } catch (Exception e) {
                        ShowUtils.errorMessage(e.toString());
                        // 发生读取错误时显示错误信息后退出系统
                        System.exit(0);
                    }
                    break;
            }
        }
    }

    public static void main(String args[]) {
        EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
