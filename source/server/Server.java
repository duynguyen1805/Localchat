import java.io.*;
import java.net.*;
import java.util.ArrayList;


public class Server {
	private Object lock;

	private ServerSocket s;
	private Socket socket;
	static ArrayList<Handler> clients = new ArrayList<Handler>();
	private String dataFile = "data\\accounts.txt";

	private void loadAccounts() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "utf8"));

			String info = br.readLine();
			while (info != null && !(info.isEmpty())) {
				clients.add(new Handler(info.split(",")[0], info.split(",")[1], false, lock));
				info = br.readLine();
			}

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveAccounts() {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new File(dataFile), "utf8");
		} catch (Exception ex ) {
			System.out.println(ex.getMessage());
		}
		for (Handler client : clients) {
			pw.print(client.getUsername() + "," + client.getPassword() + "\n");
		}
		pw.println("");
		if (pw != null) {
			pw.close();
		}
	}

	public Server() throws IOException {
		try {
			// Object dung để synchronize cho giao tiep voi cac nguoi dung khac
			lock = new Object();

			// Doc ds tai khoan da dang ky
			this.loadAccounts();
			// Socket dung de xu ly cac yeu cau login/signup tu user
			s = new ServerSocket(9999);

			while (true) {
				// Doi request login/signup tu client
				socket = s.accept();

				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				// Doc yeu cau login/signup
				String request = dis.readUTF();

				if (request.equals("Sign up")) {
					// yeu cau signup tu user

					String username = dis.readUTF();
					String password = dis.readUTF();

					// check username co ton tai chua
					if (isExisted(username) == false) {

						// Tao mot Handler de giai quyet cac request tu user nay
						Handler newHandler = new Handler(socket, username, password, true, lock);
						clients.add(newHandler);

						//luu ds tk xuong file va gui noti login thanh cong
						this.saveAccounts();
						dos.writeUTF("Sign up successful");
						dos.flush();

						//Tao mot Thread de giao tiep voi user nay
						Thread t = new Thread(newHandler);
						t.start();

						//Gui noti cho cac client dang online cap nhap danh nguoi dung truc tuyen
						updateOnlineUsers();
					} else {

						// Thong bao dang nhap that bai
						dos.writeUTF("Tên tài khoản đã tồn tại");
						dos.flush();
					}
				} else if (request.equals("Log in")) {
					// yeu cau login tu user

					String username = dis.readUTF();
					String password = dis.readUTF();

					// check username co ton tai chua
					if (isExisted(username) == true) {
						for (Handler client : clients) {
							if (client.getUsername().equals(username)) {
								// check pass co trung khop khong
								if (password.equals(client.getPassword())) {

									// Tao mot Handler de giai quyet cac request tu user nay
									Handler newHandler = client;
									newHandler.setSocket(socket);
									newHandler.setIsLoggedIn(true);

									// Thong bao login thanh cong
									dos.writeUTF("Log in successful");
									dos.flush();

									// Tao mot Thread de giao tiep voi user nay
									Thread t = new Thread(newHandler);
									t.start();

									// Gui noti cho cac client dang online cap nhap danh nguoi dung truc tuyen
									updateOnlineUsers();
								} else {
									dos.writeUTF("Mật khẩu không chính xác");
									dos.flush();
								}
								break;
							}
						}

					} else {
						dos.writeUTF("Tên tài khoản không tồn tại");
						dos.flush();
					}
				}

			}

		} catch (Exception ex){
			System.err.println(ex);
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	/** Kiểm tra username đã tồn tại hay chưa */
	public boolean isExisted(String name) {
		for (Handler client:clients) {
			if (client.getUsername().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gửi yêu cầu các user đang online cập nhật lại danh sách người dùng trực tuyến
	 * Được gọi mỗi khi có 1 user online hoặc offline
	 */
	public static void updateOnlineUsers() {
		String message = " ";
		for (Handler client:clients) {
			if (client.getIsLoggedIn() == true) {
				message += ",";
				message += client.getUsername();
			}
		}
		for (Handler client:clients) {
			if (client.getIsLoggedIn() == true) {
				try {
					client.getDos().writeUTF("Online users");
					client.getDos().writeUTF(message);
					client.getDos().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
/**
 * Luồng riêng dùng để giao tiếp với mỗi user
 */
class Handler implements Runnable{
	// Object để synchronize các hàm cần thiết
	// Các client đều có chung object này được thừa hưởng từ chính server
	private Object lock;

	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String username;
	private String password;
	private boolean isLoggedIn;

	public Handler(Socket socket, String username, String password, boolean isLoggedIn, Object lock) throws IOException {
		this.socket = socket;
		this.username = username;
		this.password = password;
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
		this.isLoggedIn = isLoggedIn;
		this.lock = lock;
	}

	public Handler(String username, String password, boolean isLoggedIn, Object lock) {
		this.username = username;
		this.password = password;
		this.isLoggedIn = isLoggedIn;
		this.lock = lock;
	}

	public void setIsLoggedIn(boolean IsLoggedIn) {
		this.isLoggedIn = IsLoggedIn;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
		try {
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Đóng socket kết nối với client
	 * Được gọi khi người dùng offline
	 */
	public void closeSocket() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean getIsLoggedIn() {
		return this.isLoggedIn;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public DataOutputStream getDos() {
		return this.dos;
	}

	@Override
	public void run() {

		while (true) {
			try {
				String message = null;

				// Doc yeu cau user
				message = dis.readUTF();

				// yeu cau dang xuat tu user
				if (message.equals("Log out")) {

					// thong bao user co the dang xuat
					dos.writeUTF("Safe to leave");
					dos.flush();

					// Dong socket, chuyen thanh trang thai offline
					socket.close();
					this.isLoggedIn = false;

					// Thong bao user khac, cap nhat danh sach truc tuyen
					Server.updateOnlineUsers();
					break;
				}

				// yeu cau gui tin nhan dang van ban
				else if (message.equals("Text")){
					String receiver = dis.readUTF();
					String content = dis.readUTF();

					for (Handler client: Server.clients) {
						if (client.getUsername().equals(receiver)) {
							synchronized (lock) {
								client.getDos().writeUTF("Text");
								client.getDos().writeUTF(this.username);
								client.getDos().writeUTF(content);
								client.getDos().flush();
								break;
							}
						}
					}
				}

				// yeu cau gui tin nhan dang Emoji
				else if (message.equals("Emoji")) {
					String receiver = dis.readUTF();
					String emoji = dis.readUTF();

					for (Handler client: Server.clients) {
						if (client.getUsername().equals(receiver)) {
							synchronized (lock) {
								client.getDos().writeUTF("Emoji");
								client.getDos().writeUTF(this.username);
								client.getDos().writeUTF(emoji);
								client.getDos().flush();
								break;
							}
						}
					}
				}

				// yeu cau gui File
				else if (message.equals("File")) {

					// Đọc các header của tin nhắn gửi file
					String receiver = dis.readUTF();
					String filename = dis.readUTF();
					int size = Integer.parseInt(dis.readUTF());
					int bufferSize = 2048;
					byte[] buffer = new byte[bufferSize];

					for (Handler client: Server.clients) {
						if (client.getUsername().equals(receiver)) {
							synchronized (lock) {
								client.getDos().writeUTF("File");
								client.getDos().writeUTF(this.username);
								client.getDos().writeUTF(filename);
								client.getDos().writeUTF(String.valueOf(size));
								while (size > 0) {
									// Gửi lần lượt từng buffer cho người nhận cho đến khi hết file
									dis.read(buffer, 0, Math.min(size, bufferSize));
									client.getDos().write(buffer, 0, Math.min(size, bufferSize));
									size -= bufferSize;
								}
								client.getDos().flush();
								break;
							}
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}