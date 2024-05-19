package zad1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    //kolorki do podkreślania konkretnych komunikatów
    // np. operacje login są na zielono, a logout na czerwono
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String MAGENTA = "\u001B[95m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    private static final String testFileName = System.getProperty("user.home") + "/ChatTest.txt";
    private static final String host = "localhost";

    private static String example1 =
            "localhost\n" +
                    "9999\n" +
                    "Asia\t50\tDzień dobry\taaaa\tbbbb\tDo widzenia\n" +
                    "Adam\t50\tDzień dobry\taaaa\tbbbb\tDo widzenia\n" +
                    "Sara\t50\tDzień dobry\taaaa\tbbbb\tDo widzenia\n";

    private static String example2 =
            "localhost\n" +
                    "33333\n" +
                    "Asia\t20\tDzień dobry\tbeee\tDo widzenia\n" +
                    "Sara\t20\tDzień dobry\tmuuu\tDo widzenia";

    private static String example3 =
            "localhost\n" +
                    "55557\n" +
                    "Asia\t10\tDzień dobry\tbeee\tDo widzenia\n" +
                    "Sara\t20\tDzień dobry\tmuuu\tDo widzenia\n";

    private static String example4 =
            "localhost\n" +
                    "55557\n" +
                    "Asia\t0\tDzień dobry\tbeee\tDo widzenia\n" +
                    "Sara\t0\tDzień dobry\tmuuu\tDo widzenia\n";

    private int words_range = 5; //do 100, ale można to sobie zmienić w metodzie newExample
    private boolean random_words_number = true;

    private int names_range = 10; //zawsze będzie przynajmniej 1, max 25(chyba)
    private boolean random_names_number = true;//no chyba że to jest ustawione na false

    private int wait_range = 0;
    private boolean random_wait = true;

    private List<Exception> exceptionList = new ArrayList<>();  // wyjątki rzucone w czasie wykonia programu
    private String serverLog = "";  // server log wyciągnięty z wykonaia programu
    private Map<String, String> clientChats = new HashMap<>();  //wiadomości wyciągnięte z wykonania programu
    private Map<String, List<String>> clientMessages = new HashMap<>(); //wiadomości wyciągnięte z plików
    private Map<String, List<MessageToken>> tokenizedExtractedUserLogs = new HashMap<>();   //wiadomości wyciągnięte z wykonanych logów serwera
    private boolean passed = true;
    private int minimalTimeExpected = 1;
    private int maximumTimeExpected = 0;

    public static void main(String[] args) {
        runAllStandardTests();

        System.exit(1);

        //uruchamia po kolei wszystkie przykłady podane na sdkp
        runAllStandardTests();

        //uruchamia kolejne przykłady wygenerowane przez program
        runGeneratedTests(50);

        //uruchamia Test na podstawie bierzącego pliku ChatTest.txt
        new Test();

        //można je na spokojnie uruchamiać w pętli
        for(int a=0;a<20;a++){
            Test test=new Test();
            if(!test.passed){
                break;
            }
        }

        //uruchamia konkretny przykład 1-4 z sdkp
        new Test(1);

        //uruchamia wygenerowny test z wybranymi wartościami (generuje nowy plik ChatTest.txt)
        int words_range = 3; //max 20 newExample
        boolean random_words_number = true;

        int names_range = 4; //zawsze będzie przynajmniej 1, max 25(chyba)
        boolean random_names_number = true;//no chyba że to jest ustawione na false

        int wait_range = 0;
        boolean random_wait = true;

        new Test(words_range,random_words_number,names_range,random_names_number,wait_range,random_wait);
        //      proponowane przypadki testowe wszystkie napisane dla testów generowanych

        //     1. words_range=4,random_words_number=false
        //    ,names_range=3,random_names_number=false
        //    ,wait_range=50,random_wait=false

        //    2. words_range=4,random_words_number=false
        //      ,names_range=3,random_names_number=false
        //      ,wait_range=20,random_wait=false

        //    3. words_range=5,random_words_number=false
        //    ,names_range=5,random_names_number=false
        //    ,wait_range=20,random_wait=true

        //    4. words_range=2,random_words_number=true
        //    ,names_range=5,random_names_number=false
        //    ,wait_range=0,random_wait=false

        //    5. words_range=10,random_words_number=true
        //    ,names_range=7,random_names_number=true
        //    ,wait_range=0,random_wait=false


        //    w momencie gdy dostaniesz informację że coś nie działa po prostu zmień flage newRunNewFile na false
        //    i odpal program ponownie

        //    PS warto ponazywać swoje wątki żeby wiedzieć co się nie zatrzymało, lub nie uruchomiło

        //    PSS program nie jest idealny więc testy z flagą newRunNewFile=true mogą czasami wyrzucać nadmiarowe błędy
        //    gdy tak się stanie zmieniamy flagę newRunNewFile na false i odpalamy ponownie, jeśli problem dalej występuje
        //    to znaczy że coś jest nie tak z twoim kodem

    }

    public static boolean runAllStandardTests() {
        boolean passed=true;
        for (int a = 1; a < 5; a++) {
            System.out.println(MAGENTA + "Running Example Test nr: " + a + "\n" + RESET);
            Test example = new Test(a);
            if (example.isPassed()) {
                System.out.println(GREEN + "Example Test nr " + a + " passed\n" + RESET);
            } else {
                passed=false;
                example.displayActiveThreads();
                System.err.println("Failed");
                break;
            }

        }
        return passed;
    }

    public static void runGeneratedTests(int num) {
        for (int a = 0; a < num; a++) {
            System.out.println(MAGENTA + "Running Generated Test nr: " + a + "\n" + RESET);
            //dodawanie 0 jest tutaj tylko po to żeby inttelij pokazał nazwy zmiennych z konstruktora
            Test example = new Test(
                    (int) (Math.random() * 20)+0, true
                    , (int) (Math.random() * 20) + 0, true
                    , (int) (Math.random() * 20) + 0, true);
            if (example.isPassed()) {
                System.out.println(GREEN + "Generated Test nr" + a + " passed\n" + RESET);
            } else {
                example.displayActiveThreads();
                System.err.println("Failed");
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Test() {
        runTest();
    }

    public Test(int example) {
        nextExample(example);
        runTest();
    }

    public Test(int words_range, boolean random_words_number, int names_range, boolean random_names_number, int wait_range, boolean random_wait) {
        this.words_range = words_range;
        this.random_words_number = random_words_number;
        this.names_range = names_range;
        this.random_names_number = random_names_number;
        this.wait_range = wait_range;
        this.random_wait = random_wait;
        nextExample();
        runTest();
    }

    public boolean isPassed() {
        return passed;
    }

    private void runTest() {
        maximumTimeExpected = minimalTimeExpected + 10000;
        parseSourceFile();
        Thread timeout = new Thread(() -> {
            Exception exc=null;
            try {
                Thread.sleep(maximumTimeExpected);
            } catch (InterruptedException e) {
                exc=e;
            }

            if (exc==null) {
                System.out.println(RED + "Timed Out" + RESET);
                displayActiveThreads();
            }
        });
        timeout.setName("timeout");
        long start = System.currentTimeMillis();
        timeout.start();
        runAndCollectResults();
        long actual_execution_time = System.currentTimeMillis() - start;
        timeout.interrupt();
        System.err.println("entry");
        checkResults();
        System.err.println("exit");

        passed = true;
        if (!exceptionList.isEmpty()) {
            passed = false;
            System.out.println("Thrown exceptions:");
            System.out.println(exceptionList);
        }

        if (actual_execution_time < minimalTimeExpected) {
            System.err.println("Program executed too fast, you didn't wait");
            System.err.println("Minimal expected time " + minimalTimeExpected + " ms");
            System.err.println("Actual execution time " + actual_execution_time + " ms");
        } else if (actual_execution_time > maximumTimeExpected) {
            passed = false;
            System.err.println("Program executed too slow");
            System.err.println("Maximum expected time " + maximumTimeExpected + " ms");
            System.err.println("Actual execution time " + actual_execution_time + " ms");
        } else {
            System.out.println(GREEN + "Actual execution time " + actual_execution_time + " ms" + RESET);
            System.out.println(GREEN + "Expected execution time " + minimalTimeExpected + " ms" + RESET);
        }

        if (passed) {
            System.out.println(GREEN + "Congrats all tests passed" + RESET);
        } else {
            System.out.println(RED+"Failed"+RESET);
            System.out.println("=== Server Log ===");
            System.out.println(serverLog);
            for (String s : clientChats.values()) {
                System.out.println(s);
            }
        }
    }

    public void runAndCollectResults() {
        try {
            String testFileName = System.getProperty("user.home") + "/ChatTest.txt";
            List<String> test = Files.readAllLines(Paths.get(testFileName));
            String host = test.remove(0);
            int port = Integer.valueOf(test.remove(0));
            ChatServer s = new ChatServer(host, port);
            s.startServer();

            ExecutorService es = Executors.newCachedThreadPool();
            List<ChatClientTask> ctasks = new ArrayList<>();

            for (String line : test) {
                String[] elts = line.split("\t");
                String id = elts[0];
                int wait = Integer.valueOf(elts[1]);
                List<String> msgs = new ArrayList<>();
                for (int i = 2; i < elts.length; i++) msgs.add(elts[i] + ", mówię ja, " + id);
                ChatClient c = new ChatClient(host, port, id);
                ChatClientTask ctask = ChatClientTask.create(c, msgs, wait);
                ctasks.add(ctask);
                es.execute(ctask);
            }
            ctasks.forEach(task -> {
                try {
                    task.get();
                } catch (InterruptedException | ExecutionException exc) {
                    exceptionList.add(exc);
                }
            });
            es.shutdown();
            s.stopServer();

            serverLog = s.getServerLog();

            ctasks.forEach(t -> clientChats.put(t.getClient().getId(), t.getClient().getChatView()));
        } catch (Exception e) {
            exceptionList.add(e);
        }
    }

    public void parseSourceFile() {
        try {
            String testFileName = System.getProperty("user.home") + "/ChatTest.txt";
            List<String> test = Files.readAllLines(Paths.get(testFileName));
            Map<String, List<String>> clients = new HashMap<>();
            String host = test.remove(0);
            int port = Integer.valueOf(test.remove(0));
            for (String line : test) {
                String[] userTokens = line.split("\t");
                String clientID = userTokens[0];
                minimalTimeExpected += Integer.valueOf(userTokens[1]);
                List<String> messages = new ArrayList<>();
                for (int a = 2; a < userTokens.length; a++) {
                    messages.add(userTokens[a]);
                }
                clients.put(clientID, messages);
            }
            clientMessages = clients;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkResults() {
        String serverResults = parseServerLog();
        String clientResults = parseUserLogs();
        if (!passed) {
            System.out.printf(serverResults);
            System.out.println(clientResults);
        }
    }

    private String parseServerLog() {
        //TODO: czy długość przewidzianych i aktualnych logów serwera się zgadza V
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("=== Server Log Tests===\n");
        resultBuilder.append("=== Server Log Length Test===\n");
        boolean logLengthTest = true;

        String[] tokenizedServerLog = serverLog.split("\n");
        int expectedServerLogLength = 0;
        for (Map.Entry<String, List<String>> entry : clientMessages.entrySet()) {
            expectedServerLogLength += entry.getValue().size() + 2;
        }

        int actualServerLogLength = tokenizedServerLog.length;
        if (actualServerLogLength != expectedServerLogLength) {
            resultBuilder.append(RED).append("Server log length differs from the expected one\n");
            resultBuilder.append("expected size = ").append(expectedServerLogLength).append("\n");
            resultBuilder.append("actual size = ").append(actualServerLogLength).append("\n").append(RESET);
            logLengthTest = false;
        } else {
            resultBuilder.append(GREEN).append("Server log length matches predicted length\n").append(RESET);
        }

        //TODO: czy serwer ma logi w odpowiednim formacie V
        boolean logFormatTest = true;
        resultBuilder.append("=== Log Format Test ===\n");
        resultBuilder.append("Checks if logs are in the specified formats: \n")
                .append("Server Log : HH:MM:SS.sss message\n")
                .append("All pattern below are only for the message part\n")
                .append("LOGIN : ID logged in\n")
                .append("LOGOUT : ID logged out\n")
                .append("MESSAGE : ID: message, mówię ja, ID\n");
        resultBuilder.append("=== Server Log ===\n");

        MessageToken msgToken;
        String type;
        List<MessageToken> wrongFormatList = new ArrayList<>();
        for (String s : tokenizedServerLog) {
            msgToken = new MessageToken(s);
            type = msgToken.getType();
            if (!type.equals("UNKNOWN") && !type.equals("WRONG FORMAT")) {
                if (!tokenizedExtractedUserLogs.containsKey(msgToken.getID())) {
                    tokenizedExtractedUserLogs.put(msgToken.getID(), new ArrayList<>());
                }
                tokenizedExtractedUserLogs.get(msgToken.getID()).add(msgToken);
                resultBuilder.append(GREEN).append(msgToken).append(RESET).append("\n");
            } else {
                resultBuilder.append(RED).append("WRONG FORMAT: ").append(msgToken).append(RESET).append("\n");
                wrongFormatList.add(msgToken);
                logFormatTest = false;
            }
        }
        resultBuilder.append("\n=== Log Format Test Results ===\n");
        if (logFormatTest) {
            resultBuilder.append(GREEN).append("All logs are in the correct format").append(RESET).append("\n");
        } else {
            resultBuilder.append(RED).append("Some of the logs doesn't follow the formats\n");
            resultBuilder.append("List of ").append(wrongFormatList.size()).append(" wrong formatted logs:\n");
            for (MessageToken mt : wrongFormatList) {
                resultBuilder.append(mt).append("\n");
            }
            resultBuilder.append(RESET).append("\n");
        }

        //TODO: czy serwer ma tyle samo użytkowników ile plik V
        boolean userListTest = true;
        resultBuilder.append("=== User List Test ===\n");
        resultBuilder.append("(This test checks if server has the same users as the file)\n");
        Set<String> serverClients = tokenizedExtractedUserLogs.keySet();
        Set<String> fileClients = clientMessages.keySet();
        if (serverClients.size() == fileClients.size()) {
            resultBuilder.append(GREEN).append("The amount of users checks up\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("The amount of users differ beetween server and file\n");
            resultBuilder.append("List of missing users:\n");
            userListTest = false;
        }

        for (String id : fileClients) {
            if (!serverClients.contains(id)) {
                resultBuilder.append(id).append("\n");
            }
        }
        resultBuilder.append(RESET);

        //TODO: czy serwer nie zawiera duplikatów logów V
        //tokenizedServerLog
        resultBuilder.append("=== Duplicates Test ===\n");
        resultBuilder.append("(This test checks if Server logs are unique)\n");
        Set<String> logSet = new HashSet<>(Arrays.asList(tokenizedServerLog));
        boolean duplicatesTest = logSet.size() == tokenizedServerLog.length;
        if (duplicatesTest) {
            resultBuilder.append(GREEN).append("Test passed. The server log does not contain any duplicates\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Test failed. The server log contains duplicates\n");
            resultBuilder.append("List of the duplicates:\n");
            for (String s : tokenizedServerLog) {
                if (logSet.contains(s)) {
                    resultBuilder.append(s).append("\n");
                }
            }
            resultBuilder.append(RESET).append("\n");
        }


        //TODO: czy serwer ma tyle samo loginów ile logoutów V
        resultBuilder.append("=== Login/Logout Test ===\n");
        resultBuilder.append("(This test checks if client has the same amount logins as logouts)\n");
        boolean inOutTest = true;
        for (Map.Entry<String, List<MessageToken>> entry : tokenizedExtractedUserLogs.entrySet()) {
            String clientID = entry.getKey();
            int logins = 0;
            int logouts = 0;
            List<MessageToken> messageTokens = entry.getValue();
            for (MessageToken mt : messageTokens) {
                if (mt.getType().equals("LOGIN")) {
                    logins++;
                } else if (mt.getType().equals("LOGOUT")) {
                    logouts++;
                }
            }
            if (logins != logouts || logins == 0 || logouts == 0) {
                inOutTest = false;
                resultBuilder.append(RED).append("Client: ").append(clientID);
                if (logins == 0) {
                    resultBuilder.append(" haven't logged in");
                } else if (logouts == 0) {
                    resultBuilder.append(" haven't logged out");
                } else if (logins > logouts) {
                    resultBuilder.append(" haven't logged out");
                } else {
                    resultBuilder.append(" haven't logged in");
                }
                resultBuilder.append(RESET);
            } else {
                resultBuilder.append(GREEN).append("All clients have logged in and logged out succefully\n").append(RESET);
            }

        }

        //TODO: czy logi serwera są w kolejności chronologicznej
        //tokenized
        resultBuilder.append("=== Chronological Logs Test ===\n");
        resultBuilder.append("(This test checks if logs are in chronological order)\n");
        boolean chronoTest = true;
        List<MessageToken> sortedLogs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String s : tokenizedServerLog) {
            sortedLogs.add(new MessageToken(s));
        }
        LocalTime lt1;
        LocalTime lt2;
        for(int a=0;a< sortedLogs.size();a++){
            if(a>0){
                lt1=sortedLogs.get(a-1).getTimeStamp();
                lt2=sortedLogs.get(a).getTimeStamp();
                if(!lt1.equals(lt2)){
                    if(!lt1.isBefore(lt2)){
                        resultBuilder.append(sortedLogs.get(a-1)).append(" is Before ").append(sortedLogs.get(a)).append("\n");
                        chronoTest=false;
                    }
                }
            }
        }

        for (MessageToken mt : sortedLogs) {
            sb.append(mt).append("\n");
        }
        //TODO: serwerlog też konwertuj
        chronoTest = sb.toString().equals(serverLog);
        if (chronoTest) {
            resultBuilder.append(GREEN).append("All logs are in chronological order\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Logs are not in chronological order\n");
            resultBuilder.append(serverLog).append(RESET);
        }

        //TODO: czy serwer nie ominął żadnej wiadomości od klienta? V
        boolean missingMessagesTest;
        if (tokenizedExtractedUserLogs != null) {
            resultBuilder.append("=== Missing Messages Test ===\n");
            resultBuilder.append("(This test checks if server missed any of the clients messages)\n");
            missingMessagesTest = true;
            int cnt = 0;
            Map<String, List<String>> missing = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : clientMessages.entrySet()) {
                String clientID = entry.getKey();
                List<String> cMsg = entry.getValue();
                if (tokenizedExtractedUserLogs.get(clientID) != null) {
                    for (MessageToken mt : tokenizedExtractedUserLogs.get(clientID)) {
                        if (!cMsg.contains(mt.getMessage())) {
                            if (!mt.toString().contains("logged")) {
                                cnt++;
                                missingMessagesTest = false;
                                if (!missing.containsKey(clientID)) {
                                    missing.put(clientID, new ArrayList<>());
                                }
                                missing.get(clientID).add(mt.toString());
                            }
                        }
                    }
                }
            }
            if (missingMessagesTest) {
                resultBuilder.append(GREEN).append("Test passed. Server didn't miss any of the messages\n").append(RESET);
            } else {
                resultBuilder.append(RED).append("Server missed a total of ").append(cnt).append(" messages\n");
                resultBuilder.append("List of the map of the missed messages per client:\n");
                for (Map.Entry<String, List<String>> entry : missing.entrySet()) {
                    resultBuilder.append(entry.getKey()).append(":\n");
                    for (String msg : entry.getValue()) {
                        resultBuilder.append("\t").append(msg).append("\n");
                    }
                }
                resultBuilder.append(RESET).append("\n");
            }
        } else {
            missingMessagesTest = false;
        }

        resultBuilder.append("\n").append(MAGENTA).append("=== Summary ===\n");
        if (logLengthTest) {
            resultBuilder.append(GREEN).append("Log Length Test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Log Length Test failed\n").append(RESET);
            passed = false;
        }

        if (logFormatTest) {
            resultBuilder.append(GREEN).append("Log Format Test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Log Format Test failed\n").append(RESET);
            passed = false;
        }

        if (userListTest) {
            resultBuilder.append(GREEN).append("User List Test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("User List Test failed\n").append(RESET);
            passed = false;
        }

        if (duplicatesTest) {
            resultBuilder.append(GREEN).append("Duplicates Test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Duplicates Test failed\n").append(RESET);
            passed = false;
        }

        if (inOutTest) {
            resultBuilder.append(GREEN).append("Login/Logout Test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Login/Logout Test failed\n").append(RESET);
            passed = false;
        }

        if (chronoTest) {
            resultBuilder.append(GREEN).append("Chronological Logs Test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Chronological Logs failed\n").append(RESET);
            passed = false;
        }

        if (missingMessagesTest) {
            resultBuilder.append(GREEN).append("Missing Logs Test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Missing Logs Test failed\n").append(RESET);
            passed = false;
        }


        return resultBuilder.toString();
    }

    private String parseUserLogs() {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("=== Client Logs Test ===\n");
        //TODO: czy klient się zalogował i wylogował V
        resultBuilder.append("=== Login/Logout Test ===\n");
        resultBuilder.append("(This test checks if client log begins and ends with his own login/logout)\n");
        boolean inOutTest = true;
        //clientChats
        for (Map.Entry<String, String> entry : clientChats.entrySet()) {
            String clientID = entry.getKey();
            String[] chat = entry.getValue().split("\n");
            resultBuilder.append("Inspecting client: ").append(clientID).append(" :\n");
            boolean clientInspection = true;
            if (!chat[1].equals(clientID + " logged in")) {
                resultBuilder.append(RED).append("Client chat doesn't begin with his own login\n").append(RESET);
                clientInspection = false;
                inOutTest = false;
            }

            if (!chat[chat.length - 1].equals(clientID + " logged out")) {
                resultBuilder.append(RED).append("Client chat doesn't end with his own logout\n");
                clientInspection = false;
                inOutTest = false;
                resultBuilder.append(chat).append("\n").append(RESET);
            }

            if (clientInspection) {
                resultBuilder.append(GREEN).append("Client chat begins and ends with his own login/logout\n").append(RESET);
            }
        }
        if (inOutTest) {
            resultBuilder.append(GREEN).append("All client chats begins and ends with his/her own logins/logouts\n").append(RESET);
        }


        //TODO: czy klient wysłał wszystkie swoje wiadomości V
        resultBuilder.append("=== Send Test ===\n");
        resultBuilder.append("(This test checks if client sent all his messages)\n");
        boolean sendTest = true;
        for (Map.Entry<String, List<String>> entry : clientMessages.entrySet()) {

            String clientID = entry.getKey();
            Pattern pattern = Pattern.compile("(\\p{L}+)(: .+| logged (in|out))", Pattern.UNICODE_CHARACTER_CLASS);
            Matcher matcher = pattern.matcher(clientChats.get(clientID));
            int actual = 0;
            while (matcher.find()) {
                if (matcher.group(1).equals(clientID)) {
                    actual++;
                }
            }

            int expected = entry.getValue().size() + 2;
            //TODO: napisz nowy przelicznik tych wartości tym razem na bzie logów serwera
            if (actual != expected) {
                resultBuilder.append(RED).append("Client: ").append(clientID).append(" did not sent all of his/her messages\n");
                resultBuilder.append("Expected: ").append(entry.getValue().size()).append("\n");
                resultBuilder.append("Actual: ").append(actual).append("\n");
                resultBuilder.append(clientChats.get(clientID)).append("\n").append(RESET);
                sendTest = false;
            }
        }
        if (sendTest) {
            resultBuilder.append(GREEN).append("Test passed.All clients have sent all of their messages\n").append(RESET);
        } else {
            System.out.println("brak opisu");
            //TODO: dodaj bardziej szczegółowe opisy błędu
        }

        //TODO: czy klient zwraca swój czat w poprawnym formacie V
        resultBuilder.append("=== Format Test ===\n");
        resultBuilder.append("(This test checks if client chat is in the correct format)\n");
        boolean formatTest = true;
        Pattern pattern = Pattern.compile("(\\p{L}+)(: .+| logged (in|out))", Pattern.UNICODE_CHARACTER_CLASS);

        for (Map.Entry<String, String> entry : clientChats.entrySet()) {
            String clientID = entry.getKey();
            boolean inspectClient = true;
            String[] chatTokens = entry.getValue().split("\n");
            if (!chatTokens[0].contains("=== " + clientID + " chat view")) {
                resultBuilder.append(RED).append("Client Log doesn't begin with \"=== ")
                        .append(clientID).append(" chat view\"\n").append(RESET);
                formatTest = false;
            }
            List<String> wrongLines = new ArrayList<>();
            for (int a = 1; a < chatTokens.length; a++) {
                Matcher matcher = pattern.matcher(chatTokens[a]);
                if (!matcher.find()) {
                    wrongLines.add(chatTokens[a]);
                    inspectClient = false;
                }
            }

            if (!inspectClient) {
                resultBuilder.append(RED).append("Client chat for ").append(clientID).append(" is not in the required format\n");
                resultBuilder.append("List of wrong formatted logs:\n");
                resultBuilder.append(String.join("\n", wrongLines)).append(RESET).append("\n\n");
            }

        }
        if (formatTest) {
            resultBuilder.append(GREEN).append("Test passed.All clients have chats in required formats\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Test failed.Client chats are not in the required format\n").append(RESET);
        }

        //TODO: czy log klienta jest zgodny z logiem serwera V
        resultBuilder.append("=== Server Compability Test ===\n");
        resultBuilder.append("(This test checks if client logs are the same as server logs)\n");
        TreeSet<MessageToken> sortedLogs = new TreeSet<>(new Comparator<MessageToken>() {
            @Override
            public int compare(MessageToken token1, MessageToken token2) {
                LocalTime timeStamp1 = token1.getTimeStamp();
                LocalTime timeStamp2 = token2.getTimeStamp();
                return timeStamp1.compareTo(timeStamp2);
            }
        });
        Arrays.stream(serverLog.split("\n")).forEach(a -> sortedLogs.add(new MessageToken(a)));
        boolean compabilityTest = true;
        for (Map.Entry<String, String> entry : clientChats.entrySet()) {
            String clientID = entry.getKey();
            List<String> clientLog = new ArrayList<>();
            String[] chatTokens = entry.getValue().replaceAll("=== " + clientID + " chat view\n", "").split("\n");
            boolean adding = false;
            boolean inspection = true;
            for (MessageToken mt : sortedLogs) {
                if (mt.toString().equals(chatTokens[0])) {
                    adding = true;
                }
                if (mt.toString().equals(chatTokens[chatTokens.length - 1])) {
                    adding = false;
                }
                if (adding) {
                    clientLog.add(mt.toString().substring(13));
                }
            }

            resultBuilder.append(clientID).append(" logs:\n");
            for (int a = 0; a < clientLog.size() && a < chatTokens.length; a++) {
                if (clientLog.get(a).equals(chatTokens[a])) {
                    resultBuilder.append(GREEN).append(clientLog.get(a)).append("\n").append(RESET);
                } else {
                    resultBuilder.append(RED).append(clientLog.get(a)).append("\n").append(RESET);
                    inspection = false;
                    compabilityTest = false;
                }
            }

            if (inspection) {
                resultBuilder.append(GREEN).append("Logs for ").append(clientID).append(" are correct\n").append(RESET);
            } else {
                resultBuilder.append(RED).append("Logs for ").append(clientID).append(" are incorrect\n").append(RESET);
            }

        }

        if (compabilityTest) {
            resultBuilder.append(GREEN).append("Server Compability test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Server Compability test failed\n").append(RESET);
            passed = false;
        }

        if (formatTest) {
            resultBuilder.append(GREEN).append("Format test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Format test failed\n").append(RESET);
            passed = false;
        }

        if (inOutTest) {
            resultBuilder.append(GREEN).append("Login/Logout test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Login/Logout test failed\n").append(RESET);
            passed = false;
        }

        if (sendTest) {
            resultBuilder.append(GREEN).append("Send test passed\n").append(RESET);
        } else {
            resultBuilder.append(RED).append("Send test failed\n").append(RESET);
            passed = false;
        }

        return resultBuilder.toString();
    }

    public void nextExample(int example_num) {
        String example = "";
        switch (example_num) {
            case 1: {
                example = example1;
                break;
            }
            case 2: {
                example = example2;
                break;
            }
            case 3: {
                example = example3;
                break;
            }
            case 4: {
                example = example4;
                break;
            }
        }
        try {
            clearAndWriteToFile(testFileName, example);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void nextExample() {
        List<String> names = new ArrayList<>();
        names.add("Anna");
        names.add("Piotr");
        names.add("Katarzyna");
        names.add("Jan");
        names.add("Magdalena");
        names.add("Zofia");
        names.add("Jakub");
        names.add("Aleksander");
        names.add("Julia");
        names.add("Mikołaj");
        names.add("Zuzanna");
        names.add("Szymon");
        names.add("Maria");
        names.add("Filip");
        names.add("Wiktoria");
        names.add("Kacper");
        names.add("Aleksandra");
        names.add("Antoni");
        names.add("Natalia");
        names.add("Michał");
        names.add("Oliwia");
        names.add("Tomasz");
        names.add("Emilia");
        names.add("Marcin");
        names.add("Kinga");

        int namesNumber = random_names_number ? (int) (Math.random() * names_range) + 1 : names_range;


        System.out.println("Names number: "+namesNumber);
        System.out.println("Words range: "+words_range);

        List<String> words = new ArrayList<>();
        for (int i = 1; i <= namesNumber*words_range+namesNumber; i++) {
            words.add("Słowo" + i);
        }



        int port = (int) (Math.random() * 65535); // 2^16-1
        StringBuilder sb = new StringBuilder();
        sb.append(host).append("\n").append(port).append("\n");

        for (int a = 0; a < namesNumber; a++) {
            int wordsNumber = random_words_number ? (int) (Math.random() * words_range) : words_range;
            if(wordsNumber>20){
                wordsNumber=10;
            }

            sb.append(names.remove((int) (Math.random() * names.size())));
            sb.append("\t").append(random_wait ? (int) (Math.random() * wait_range) : wait_range);

            for (int b = 0; b < wordsNumber; b++) {
                sb.append("\t").append(words.remove((int) (Math.random() * words.size())));
            }
            sb.append("\n");
        }
        try {
            clearAndWriteToFile(testFileName, sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearAndWriteToFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);
        Files.write(path, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void showChars(int bytes) {
        StringBuilder sb = new StringBuilder();
        String temp = "[%s]=%d, ";
        for (int b = 0; b < Math.pow(2, bytes); b++) {
            sb.append(String.format(temp, (char) b, b));
            if (b % 8 == 0) {
                sb.append("\n");
            }
        }
        System.out.println(sb);
    }

    public void displayActiveThreads() {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }

        Thread[] threads = new Thread[rootGroup.activeCount() * 2];
        int count = rootGroup.enumerate(threads, true);

        System.out.println("Aktualnie działające wątki:");
        for (int i = 0; i < count; i++) {
            Thread t = threads[i];
            System.out.println("Wątek: " + t.getName() + " | Stan: " + t.getState());
        }
    }
}

class MessageToken {
    private LocalTime timeStamp;
    private String ID;
    private String message;
    private String type;
    private int byteSize;

    // HH:MM:SS.sss message 1-4 czas 5-wiadomość
    private static final Pattern logPattern = Pattern.compile("(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})(\\s.+)");
    // ID logged in 1-ID
    private static final Pattern loginPattern = Pattern.compile("(\\p{L}+) logged in", Pattern.UNICODE_CHARACTER_CLASS);
    // ID logged out 1-ID
    private static final Pattern logoutPattern = Pattern.compile("(\\p{L}+) logged out", Pattern.UNICODE_CHARACTER_CLASS);
    // ID: message 1-ID 2-message 3-ID
    private static final Pattern messagePattern = Pattern.compile("(\\p{L}+): (.+), mówię ja, \\1", Pattern.UNICODE_CHARACTER_CLASS);

    public MessageToken(String text) {
        Matcher logMatcher = logPattern.matcher(text);
        this.byteSize = text.getBytes(StandardCharsets.UTF_8).length;
        if (logMatcher.find()) {
            this.timeStamp = textToTime(logMatcher.group(1));
            Matcher loginMatcher = loginPattern.matcher(text);
            Matcher logoutMatcher = logoutPattern.matcher(text);
            Matcher messageMatcher = messagePattern.matcher(text);
            if (loginMatcher.find()) {
                this.type = "LOGIN";
                this.ID = loginMatcher.group(1);
                this.message = "";
            } else if (logoutMatcher.find()) {
                this.type = "LOGOUT";
                this.ID = logoutMatcher.group(1);
                this.message = "";
            } else if (messageMatcher.find()) {
                this.type = "MESSAGE";
                this.ID = messageMatcher.group(1);
                this.message = messageMatcher.group(2);
            } else {
                this.type = "UNKNOWN";
                this.ID = "UNKNOWN";
                this.message = logMatcher.group(2);
            }
        } else {
            this.type = "WRONG FORMAT";
            this.ID = "UNKNOWN";
            this.message = text;
            this.timeStamp = null;
        }
    }

    private LocalTime textToTime(String text) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        return LocalTime.parse(text, formatter);
    }

    public LocalTime getTimeStamp() {
        return timeStamp;
    }

    public String getID() {
        return ID;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public int getByteSize() {
        return byteSize;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        switch (type) {
            case "LOGIN": {
                sb.append(timeStamp.format(formatter)).append(" ").append(ID).append(" logged in");
                break;
            }

            case "LOGOUT": {
                sb.append(timeStamp.format(formatter)).append(" ").append(ID).append(" logged out");
                break;
            }

            case "MESSAGE": {
                sb.append(timeStamp.format(formatter)).append(" ")
                        .append(ID).append(": ").append(message).append(", mówię ja, ").append(ID);
                break;
            }

            default: {
                if (timeStamp != null) {
                    sb.append(timeStamp.format(formatter)).append(" ");
                }
                sb.append(message);
                break;
            }
        }
        return sb.toString();
    }

}
