import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class SignupFrame extends JFrame {

    //private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JPasswordField txtComfirmPassword, confirm;
    private String username;

    private String host = "localhost";
    private int port = 9999;
    private Socket socket;

    private DataInputStream dis;
    private DataOutputStream dos;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    SignupFrame frame = new SignupFrame();
                    frame.setVisible(true);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public SignupFrame() {
        setTitle("LOCAL CHAT");

        setDefaultLookAndFeelDecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(230, 240, 247));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(160,190,223));

        JLabel lbUsername = new JLabel("Username");
        lbUsername.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lbPassword = new JLabel("Password");
        lbPassword.setFont(new Font("Arial", Font.BOLD, 14));

        JLabel lbComfirmPassword = new JLabel("Comfirm Password");
        lbComfirmPassword.setFont(new Font("Arial", Font.BOLD, 14));

        txtUsername = new JTextField();
        txtUsername.setColumns(10);

        txtPassword = new JPasswordField();
        txtComfirmPassword = new JPasswordField();

        JPanel buttons = new JPanel();
        buttons.setBackground(new Color(230, 240, 247));
        JPanel notificationContainer = new JPanel();
        notificationContainer.setBackground(new Color(230, 240, 247));

        GroupLayout gl_contentPane = new GroupLayout(contentPane);

        gl_contentPane.setHorizontalGroup(
                gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(headerPanel, GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addGap(69)
                                .addGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING)

                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lbComfirmPassword)
                                                .addGap(18)
                                                .addComponent(txtComfirmPassword, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addComponent(lbUsername)
                                                .addGap(77)
                                                .addComponent(txtUsername, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                                        .addGroup(gl_contentPane.createSequentialGroup()
                                                .addComponent(lbPassword)
                                                .addGap(80)
                                                .addComponent(txtPassword, GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)))

                                .addGap(81))
                        .addComponent(notificationContainer, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
                        .addComponent(buttons, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
        );

        gl_contentPane.setVerticalGroup(
                gl_contentPane.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(gl_contentPane.createSequentialGroup()
                                .addComponent(headerPanel, GroupLayout.PREFERRED_SIZE, 37, GroupLayout.PREFERRED_SIZE)
                                .addGap(33)
                                .addGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lbUsername)
                                        .addComponent(txtUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(30)
                                .addGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lbPassword))
                                .addGap(30)
                                .addGroup(gl_contentPane.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(txtComfirmPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lbComfirmPassword))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(notificationContainer, GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(buttons, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
                                .addGap(22))
        );

        JLabel notification = new JLabel("");
        notification.setForeground(Color.RED);
        notification.setFont(new Font("Times New Roman", Font.PLAIN, 13));
        notificationContainer.add(notification);

        //JButton login = new JButton("Log in");
        JButton signup = new JButton("Sign up");

        signup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (String.copyValueOf(txtComfirmPassword.getPassword()).equals(String.copyValueOf(txtPassword.getPassword()))){
                    String response = Signup(txtUsername.getText(), String.copyValueOf(txtPassword.getPassword()));

                    if ( response.equals("Sign up successful") ) {
                        username = txtUsername.getText();
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                try {
                                    // In ra thông báo đăng kí thành công
                                    int confirm = JOptionPane.showConfirmDialog(null, "Sign up successful\nWelcome to MANGO CHAT", "Sign up successful", JOptionPane.DEFAULT_OPTION);

                                    ChatFrame frame = new ChatFrame(username, dis, dos);
                                    frame.setVisible(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        dispose();
                    } else {
                        //login.setEnabled(false);
                        signup.setEnabled(false);
                        txtPassword.setText("");
                        notification.setText(response);
                    }

                } else {
                    notification.setText("Confirm password does not match");
                }
            }
        });
        signup.setEnabled(false);
        buttons.add(signup);

        JLabel headerContent = new JLabel("SIGNUP");
        headerContent.setFont(new Font("Poor Richard", Font.BOLD, 24));
        headerPanel.add(headerContent);
        contentPane.setLayout(gl_contentPane);

        txtUsername.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (txtUsername.getText().isBlank() || String.copyValueOf(txtPassword.getPassword()).isBlank() || String.copyValueOf(txtPassword.getPassword()).isBlank()) {
                    //login.setEnabled(false);
                    signup.setEnabled(false);
                } else {
                    //login.setEnabled(true);
                    signup.setEnabled(true);
                }
            }
        });

        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (txtUsername.getText().isBlank() || String.copyValueOf(txtPassword.getPassword()).isBlank() || String.copyValueOf(txtPassword.getPassword()).isBlank()) {
                    signup.setEnabled(false);
                } else {
                    signup.setEnabled(true);
                }
            }
        });

        txtComfirmPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (txtUsername.getText().isBlank() || String.copyValueOf(txtPassword.getPassword()).isBlank() || String.copyValueOf(txtComfirmPassword.getPassword()).isBlank()) {
                    signup.setEnabled(false);
                } else {
                    signup.setEnabled(true);
                }
            }
        });
        this.getRootPane().setDefaultButton(signup);
    }

    public String Signup(String username, String password) {
        try {
            Connect();

            dos.writeUTF("Sign up");
            dos.writeUTF(username);
            dos.writeUTF(password);
            dos.flush();

            String response = dis.readUTF();
            return response;

        } catch (IOException e) {
            e.printStackTrace();
            return "Network error: Sign up fail";
        }
    }

    public void Connect() {
        try {
            if (socket != null) {
                socket.close();
            }
            socket = new Socket(host, port);
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getUsername() {
        return this.username;
    }
}
