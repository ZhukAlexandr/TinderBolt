package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "firstJavaBotTelegramAITinder_bot"; // добавь имя бота в кавычках
    public static String TELEGRAM_BOT_TOKEN; // добавь токен бота в кавычках
    public static String OPEN_AI_TOKEN; // добавь токен ChatGPT в кавычках

    private final ChatGPTService chatGPT;

    private DialogMode currentMode = null;

    private final ArrayList<String> list = new ArrayList<>();

    private UserInfo me;
    private UserInfo she;
    private int questionCount;


    static {
        ConfigLoader configLoader = new ConfigLoader();
        TELEGRAM_BOT_TOKEN = configLoader.getProperty("TELEGRAM_BOT_TOKEN");
        OPEN_AI_TOKEN = configLoader.getProperty("OPEN_AI_TOKEN");
    }


    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
        this.chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    }


    @Override
    public void onUpdateEventReceived(Update update) {
        String message = getMessageText();

        if (message.equals("/start")) {
            initializeMainMenu();
            return;
        }

        if (message.equals("/gpt")) {
            initializeGPTMode();
            return;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) {
            handleGPTMessage(message);
            return;
        }

        if (message.equals("/date")) {
            initializeDateMode();
            return;
        }

        if (currentMode == DialogMode.DATE && !isMessageCommand()) {
            handleDateMessage(message);
            return;
        }

        if (message.equals("/message")) {
            initializeMessageMode();
            return;
        }

        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
            handleMessageMode(message);
            return;
        }

        if (message.equals("/profile")) {
            initializeProfileMode();
            return;
        }

        if (currentMode == DialogMode.PROFILE && !isMessageCommand()) {
            handleProfileMessage(message);
            return;
        }

        if (message.equals("/opener")) {
            initializeOpenerMode();
            return;
        }

        if (currentMode == DialogMode.OPENER && !isMessageCommand()) {
            handleOpenerMessage(message);
        }
    }

    private void initializeMainMenu() {
        currentMode = DialogMode.MAIN;
        sendPhotoMessage("main");
        String text = loadMessage("main");
        sendTextMessage(text);
        showMainMenu("главное меню бота", "/start",
                "генерация Tinder-профиля \uD83D\uDE0E", "/profile",
                "сообщение для знакомства \uD83E\uDD70", "/opener",
                "переписка от вашего имени \uD83D\uDE08", "/message",
                "переписка со звездами \uD83D\uDD25", "/date",
                "задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
    }

    private void initializeGPTMode() {
        currentMode = DialogMode.GPT;
        sendPhotoMessage("gpt");
        String text = loadMessage("gpt");
        sendTextMessage(text);
    }

    private void handleGPTMessage(String message) {
        String prompt = loadPrompt("gpt");
        Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
        String answer = chatGPT.sendMessage(prompt, message);
        updateTextMessage(msg, answer);
    }

    private void initializeDateMode() {
        currentMode = DialogMode.DATE;
        sendPhotoMessage("date");
        String text = loadMessage("date");
        sendTextButtonsMessage(text,
                "Ариана Гранде", "date_grande",
                "Марго Робби", "date_robbie",
                "Зендея", "date_zendaya",
                "Райн Гослинг", "date_gosling",
                "Том Харди", "date_hardy");
    }

    private void handleDateMessage(String message) {
        String query = getCallbackQueryButtonKey();
        if (query != null && query.startsWith("date_")) {
            sendPhotoMessage(query);
            sendTextMessage("Отличный выбор! \nТвоя задача пригласить девушку на свидание за 5 сообщений.");

            String prompt = loadPrompt(query);
            chatGPT.setPrompt(prompt);
        } else {
            Message msg = sendTextMessage("Подождите, девушка набирает текст");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(msg, answer);
        }
    }

    private void initializeMessageMode() {
        currentMode = DialogMode.MESSAGE;
        sendPhotoMessage("message");
        sendTextButtonsMessage("Пришлите в чат вашу переписку",
                "Следующее сообщение", "message_next",
                "Пригласить на свидание", "message_date");
    }

    private void handleMessageMode(String message) {
        String query = getCallbackQueryButtonKey();
        if (query != null && query.startsWith("message_")) {
            String prompt = loadPrompt(query);
            String userChatHistory = String.join("\n\n", list);

            Message msg = sendTextMessage("Подождите пару секунд - chatGPT думает...");
            String answer = chatGPT.sendMessage(prompt, userChatHistory);
            updateTextMessage(msg, answer);
        } else {
            list.add(message);
        }
    }

    private void initializeProfileMode() {
        currentMode = DialogMode.PROFILE;
        sendPhotoMessage("profile");
        me = new UserInfo();
        questionCount = 1;
        sendTextMessage("Сколько вам лет?");
    }

    private void handleProfileMessage(String message) {
        switch (questionCount) {
            case 1 -> {
                me.age = message;
                questionCount = 2;
                sendTextMessage("Кем вы работаете?");
            }
            case 2 -> {
                me.occupation = message;
                questionCount = 3;
                sendTextMessage("У вас есть хобби?");
            }
            case 3 -> {
                me.hobby = message;
                questionCount = 4;
                sendTextMessage("Что вам НЕ нравится в людях?");
            }
            case 4 -> {
                me.annoys = message;
                questionCount = 5;
                sendTextMessage("Цель знакомства");
            }
            case 5 -> {
                me.goals = message;
                String aboutMyself = me.toString();
                String prompt = loadPrompt("profile");
                Message msg = sendTextMessage("Подождите пару секунд - chatGPT думает...");
                String answer = chatGPT.sendMessage(prompt, aboutMyself);
                updateTextMessage(msg, answer);
            }
        }
    }

    private void initializeOpenerMode() {
        currentMode = DialogMode.OPENER;
        sendPhotoMessage("opener");
        she = new UserInfo();
        questionCount = 1;
        sendTextMessage("Имя девушки?");
    }

    private void handleOpenerMessage(String message) {
        switch (questionCount) {
            case 1 -> {
                she.name = message;
                questionCount = 2;
                sendTextMessage("Сколько ей лет?");
            }
            case 2 -> {
                she.age = message;
                questionCount = 3;
                sendTextMessage("Кем она работает?");
            }
            case 3 -> {
                she.occupation = message;
                questionCount = 4;
                sendTextMessage("Какие у нее хобби?");
            }
            case 4 -> {
                she.hobby = message;
                questionCount = 5;
                sendTextMessage("Что ей НЕ нравится в людях?");
            }
            case 5 -> {
                she.annoys = message;
                questionCount = 6;
                sendTextMessage("Цель знакомства");
            }
            case 6 -> {
                she.goals = message;
                String aboutFriend = she.toString();
                String prompt = loadPrompt("opener");
                Message msg = sendTextMessage("Подождите пару секунд - chatGPT думает...");
                String answer = chatGPT.sendMessage(prompt, aboutFriend);
                updateTextMessage(msg, answer);
            }
        }
    }

    public static void main(String[] args) throws TelegramApiException {

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
