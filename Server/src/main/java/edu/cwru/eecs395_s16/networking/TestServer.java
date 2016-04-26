package edu.cwru.eecs395_s16.networking;

public class TestServer {

    //    static Map<UUID, Player> sessionMap;
    //
    //    public static final int port = 4567;
    //
    //    public static void main(String[] args) {
    //
    //        Configuration config = new Configuration();
    //        config.setHostname("localhost");
    //        config.setContext("/StrategyGame");
    //        config.setPort(port);
    //        InMemoryPlayerRepository userRepo = new InMemoryPlayerRepository();
    //
    //        sessionMap = new HashMap<>();
    //
    //        final SocketIOServer server = new SocketIOServer(config);
    //        DataListener<LoginUserRequest> userManagement = (client, data, ackSender) -> {
    //            System.out.println("Attempting to Auth player connection.");
    //            Player p;
    //            p = userRepo.loginPlayer(data.getUsername(), data.getPassword());
    //            UUID token = client.getSessionId();
    //            if (p != null) {
    //                sessionMap.put(token, p);
    //            } else {
    //                sessionMap.remove(token);
    //            }
    //            Map<String,Object> mp = new HashMap<>();
    //            if(p == null){
    //                mp.put("status",StatusCode.UNPROCESSABLE_DATA.code);
    //                mp.put("message","Unable to login user.");
    //            } else {
    //                mp.put("status",StatusCode.OK.code);
    //            }
    //            ackSender.sendAckData(mp);
    //        };
    //        server.addEventListener("login", LoginUserRequest.class, NetworkingInterface.getLoginHandler(userRepo,
    // sessionMap));
    //        server.addEventListener("register",RegisterUserRequest.class,(client,data,ackSender) -> {
    //            try{
    //                Player p = userRepo.registerUser(data.getUsername(), data.getPassword(), data
    // .getPasswordConfirm());
    //                Map<String,Object> mp = new HashMap<>();
    //                mp.put("status",StatusCode.OK.code);
    //                ackSender.sendAckData(mp);
    //            }catch(MismatchedPasswordException e){
    //                Map<String,Object> mp = new HashMap<>();
    //                mp.put("status",StatusCode.UNPROCESSABLE_DATA);
    //                mp.put("message","Unable to register user. Passwords do not match.");
    //                ackSender.sendAckData(mp);
    //            } catch(DuplicateUsernameException e){
    //                Map<String,Object> mp = new HashMap<>();
    //                mp.put("status",StatusCode.UNPROCESSABLE_DATA);
    //                mp.put("message","Unable to register user. Username is already taken.");
    //                ackSender.sendAckData(mp);
    //            }
    //        });
    //        server.start();
    //        System.out.println("Here is after we have started the server.");
    //    }

}
