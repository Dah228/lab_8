package client.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * Менеджер локализации для поддержки 4 языков:
 * ru_RU, nl_NL, da_DK, en_IN
 *
 * Ресурсы хранятся ВНУТРИ класса (не во внешних файлах).
 * Поддерживает форматирование чисел, дат и времени.
 * Уведомляет об изменениях через ObjectProperty<Locale>.
 */
public class LocalizationManager {

    public static final Locale RU = new Locale("ru", "RU");
    public static final Locale NL = new Locale("nl", "NL");
    public static final Locale DA = new Locale("da", "DK");
    public static final Locale EN_IN = new Locale("en", "IN");

    public static final List<Locale> SUPPORTED_LOCALES = List.of(RU, NL, DA, EN_IN);


    private final Map<Locale, Map<String, String>> resources = new HashMap<>();

    private final ObjectProperty<Locale> currentLocale = new SimpleObjectProperty<>(RU);

    private NumberFormat numberFormat;
    private NumberFormat currencyFormat;
    private DateTimeFormatter dateFormatter;
    private DateTimeFormatter dateTimeFormatter;
    private DateTimeFormatter timeFormatter;

    public LocalizationManager() {
        initializeResources();
        updateFormatters();


        currentLocale.addListener((obs, oldLocale, newLocale) -> {
            updateFormatters();
        });
    }


    private void initializeResources() {
        // RU
        resources.put(RU, Map.ofEntries(

                Map.entry("table.filter.all_types", "Все типы"),
                Map.entry("table.filter.all_fuels", "Все виды топлива"),

                Map.entry("filter.all_types", "Все типы"),  // для RU
                Map.entry("filter.all_fuels", "Все топлива"),

                Map.entry("app.title", "Vehicle Manager"),
                Map.entry("confirm.exit", "Вы действительно хотите выйти?"),
                Map.entry("auth.login", "Логин"),
                Map.entry("auth.password", "Пароль"),
                Map.entry("auth.register", "Регистрация"),
                Map.entry("auth.login.button", "Войти"),
                Map.entry("auth.register.button", "Зарегистрироваться"),
                Map.entry("auth.error.empty", "Логин и пароль не могут быть пустыми"),
                Map.entry("auth.error.auth", "Неверный логин или пароль"),
                Map.entry("auth.error.register", "Ошибка регистрации"),
                Map.entry("main.user.label", "Пользователь:"),
                Map.entry("main.table.title", "Список объектов"),
                Map.entry("main.visual.title", "Визуализация"),
                Map.entry("main.lang.label", "Язык:"),
                Map.entry("table.filter", "Фильтр"),
                Map.entry("table.sort.asc", "По возрастанию"),
                Map.entry("table.sort.desc", "По убыванию"),
                Map.entry("table.column.id", "ID"),
                Map.entry("table.column.name", "Имя"),
                Map.entry("table.column.x", "X"),
                Map.entry("table.column.y", "Y"),
                Map.entry("table.column.date", "Дата создания"),
                Map.entry("table.column.power", "Мощность"),
                Map.entry("table.column.distance", "Дистанция"),
                Map.entry("table.column.type", "Тип"),
                Map.entry("table.column.fuel", "Топливо"),
                Map.entry("table.column.price", "Цена"),
                Map.entry("table.column.owner", "Владелец"),
                Map.entry("vehicle.edit", "Редактировать"),
                Map.entry("vehicle.delete", "Удалить"),
                Map.entry("vehicle.buy", "Купить"),
                Map.entry("dialog.save", "Сохранить"),
                Map.entry("dialog.cancel", "Отмена"),
                Map.entry("error.network", "Ошибка сети"),
                Map.entry("error.server", "Ошибка сервера"),
                Map.entry("success.saved", "Сохранено"),
                Map.entry("success.deleted", "Удалено"),
                Map.entry("confirm.delete", "Подтвердите удаление объекта"),
                // Для RU (в Map.ofEntries после существующих записей):
                Map.entry("app.status.initializing", "Инициализация подключения..."),
                Map.entry("app.status.connecting", "Подключение к серверу..."),
                Map.entry("app.status.loading_commands", "Загрузка команд..."),
                Map.entry("app.status.ready", "Готово!"),
                Map.entry("app.status.connected", "Подключено"),
                Map.entry("app.commands_loaded", "Загружено команд"),
                Map.entry("app.status.error", "Ошибка"),
                Map.entry("app.status.disconnecting", "Закрытие соединения..."),
                Map.entry("error.handshake", "Сервер не ответил на рукопожатие"),
                Map.entry("error.commands_load", "Ошибка загрузки карты команд"),
                Map.entry("error.unknown", "Неизвестная ошибка"),

                Map.entry("vehicle.info", "Информация об объекте"),

                Map.entry("btn.show", "Показать"),
                Map.entry("btn.add", "Добавить"),
                Map.entry("btn.update", "Обновить"),
                Map.entry("btn.remove", "Удалить по ID"),
                Map.entry("btn.clear", "Очистить"),
                Map.entry("btn.info", "Инфо"),
                Map.entry("btn.sort", "Сортировать"),
                Map.entry("btn.print_desc", "Печать (по убыванию)"),
                Map.entry("btn.shuffle", "Перемешать"),
                Map.entry("btn.filter_engine", "Фильтр по мощности"),
                Map.entry("btn.buy", "Купить"),
                Map.entry("btn.balance", "Баланс"),
                Map.entry("btn.deposit", "Пополнить"),
                Map.entry("btn.help", "Помощь"),
                Map.entry("btn.exit", "Выход"),
                Map.entry("col.id", "ID"),
                Map.entry("col.name", "Имя"),
                Map.entry("col.coords", "Координаты (X,Y)"),
                Map.entry("col.creation_date", "Дата создания"),
                Map.entry("col.engine_power", "Мощность"),
                Map.entry("col.distance", "Дистанция"),
                Map.entry("col.type", "Тип"),
                Map.entry("col.fuel", "Топливо"),
                Map.entry("col.owner", "Владелец"),
                Map.entry("col.price", "Цена"),
                Map.entry("filter.id", "ID"),
                Map.entry("filter.name", "Имя"),
                Map.entry("filter.owner", "Владелец"),
                Map.entry("filter.min_price", "Мин. цена"),
                Map.entry("filter.max_price", "Макс. цена"),
                Map.entry("filter.type", "Тип"),
                Map.entry("filter.fuel", "Топливо"),

                Map.entry("table.refresh", "Обновить")
        ));

        // NL
        resources.put(NL, Map.ofEntries(

                Map.entry("table.filter.all_types", "Alle typen"),
                Map.entry("table.filter.all_fuels", "Alle brandstoffen"),

                Map.entry("filter.all_types", "Alle typen"),
                Map.entry("filter.all_fuels", "Alle brandstoffen"),

                Map.entry("app.title", "Voertuigbeheer"),
                Map.entry("auth.login", "Gebruikersnaam"),
                Map.entry("auth.password", "Wachtwoord"),
                Map.entry("auth.register", "Registreren"),
                Map.entry("auth.login.button", "Inloggen"),

                Map.entry("table.refresh", "Vernieuwen"),
                Map.entry("vehicle.info", "Objectinformatie"),


                // Status en fouten
                Map.entry("app.status.initializing", "Verbinding initialiseren..."),
                Map.entry("app.status.connecting", "Verbinden met server..."),
                Map.entry("app.status.loading_commands", "Commando's laden..."),
                Map.entry("app.status.ready", "Klaar!"),
                Map.entry("app.status.connected", "Verbonden"),
                Map.entry("app.commands_loaded", "Commando's geladen"),
                Map.entry("app.status.error", "Fout"),
                Map.entry("app.status.disconnecting", "Verbinding sluiten..."),
                Map.entry("error.handshake", "Server reageerde niet op handshake"),
                Map.entry("error.commands_load", "Fout bij laden van commando's"),
                Map.entry("error.unknown", "Onbekende fout"),

                Map.entry("confirm.exit", "Weet u zeker dat u wilt afsluiten?"),

                Map.entry("btn.show", "Tonen"),
                Map.entry("btn.add", "Toevoegen"),
                Map.entry("btn.update", "Bijwerken"),
                Map.entry("btn.remove", "Verwijderen op ID"),
                Map.entry("btn.clear", "Wissen"),
                Map.entry("btn.info", "Info"),
                Map.entry("btn.sort", "Sorteren"),
                Map.entry("btn.print_desc", "Afdalen weergeven"),
                Map.entry("btn.shuffle", "Mengen"),
                Map.entry("btn.filter_engine", "Filter op vermogen"),
                Map.entry("btn.buy", "Kopen"),
                Map.entry("btn.balance", "Balans"),
                Map.entry("btn.deposit", "Storten"),
                Map.entry("btn.help", "Help"),
                Map.entry("btn.exit", "Afsluiten"),
                Map.entry("col.id", "ID"),
                Map.entry("col.name", "Naam"),
                Map.entry("col.coords", "Coördinaten (X,Y)"),
                Map.entry("col.creation_date", "Aanmaakdatum"),
                Map.entry("col.engine_power", "Vermogen"),
                Map.entry("col.distance", "Afstand"),
                Map.entry("col.type", "Type"),
                Map.entry("col.fuel", "Brandstof"),
                Map.entry("col.owner", "Eigenaar"),
                Map.entry("col.price", "Prijs"),
                Map.entry("filter.id", "ID"),
                Map.entry("filter.name", "Naam"),
                Map.entry("filter.owner", "Eigenaar"),
                Map.entry("filter.min_price", "Min. prijs"),
                Map.entry("filter.max_price", "Max. prijs"),
                Map.entry("filter.type", "Type"),
                Map.entry("filter.fuel", "Brandstof"),

                Map.entry("auth.register.button", "Registreren"),
                Map.entry("auth.error.empty", "Gebruikersnaam en wachtwoord mogen niet leeg zijn"),
                Map.entry("auth.error.auth", "Onjuiste gebruikersnaam of wachtwoord"),
                Map.entry("auth.error.register", "Registratiefout"),
                Map.entry("main.user.label", "Gebruiker:"),
                Map.entry("main.table.title", "Objectenlijst"),
                Map.entry("main.visual.title", "Visualisatie"),
                Map.entry("main.lang.label", "Taal:"),
                Map.entry("table.filter", "Filter"),
                Map.entry("table.sort.asc", "Oplopend"),
                Map.entry("table.sort.desc", "Aflopend"),
                Map.entry("table.column.id", "ID"),
                Map.entry("table.column.name", "Naam"),
                Map.entry("table.column.x", "X"),
                Map.entry("table.column.y", "Y"),
                Map.entry("table.column.date", "Aanmaakdatum"),
                Map.entry("table.column.power", "Vermogen"),
                Map.entry("table.column.distance", "Afstand"),
                Map.entry("table.column.type", "Type"),
                Map.entry("table.column.fuel", "Brandstof"),
                Map.entry("table.column.price", "Prijs"),
                Map.entry("table.column.owner", "Eigenaar"),
                Map.entry("vehicle.edit", "Bewerken"),
                Map.entry("vehicle.delete", "Verwijderen"),
                Map.entry("vehicle.buy", "Kopen"),
                Map.entry("dialog.save", "Opslaan"),
                Map.entry("dialog.cancel", "Annuleren"),
                Map.entry("error.network", "Netwerkfout"),
                Map.entry("error.server", "Serverfout"),
                Map.entry("success.saved", "Opgeslagen"),
                Map.entry("success.deleted", "Verwijderd"),
                Map.entry("confirm.delete", "Bevestig verwijdering van object")
        ));

        // DA
        // DA
        resources.put(DA, Map.ofEntries(
                Map.entry("app.title", "Køretøjsstyring"),
                Map.entry("auth.login", "Brugernavn"),
                Map.entry("auth.password", "Adgangskode"),
                Map.entry("auth.register", "Registrer"),
                Map.entry("auth.login.button", "Log ind"),
                Map.entry("auth.register.button", "Registrer"),

                Map.entry("table.filter.all_types", "Alle typer"),
                Map.entry("table.filter.all_fuels", "Alle brændstoffer"),

                Map.entry("filter.all_types", "Alle typer"),
                Map.entry("filter.all_fuels", "Alle brændstoffer"),

                Map.entry("vehicle.info", "Objektinformation"),

                Map.entry("btn.show", "Vis"),
                Map.entry("btn.add", "Tilføj"),
                Map.entry("btn.update", "Opdater"),
                Map.entry("btn.remove", "Fjern efter ID"),
                Map.entry("btn.clear", "Ryd"),
                Map.entry("btn.info", "Info"),
                Map.entry("btn.sort", "Sorter"),
                Map.entry("btn.print_desc", "Udskriv faldende"),
                Map.entry("btn.shuffle", "Bland"),
                Map.entry("btn.filter_engine", "Filtrer efter effekt"),
                Map.entry("btn.buy", "Køb"),
                Map.entry("btn.balance", "Balance"),
                Map.entry("btn.deposit", "Indsæt"),
                Map.entry("btn.help", "Hjælp"),
                Map.entry("btn.exit", "Afslut"),
                Map.entry("col.id", "ID"),
                Map.entry("col.name", "Navn"),
                Map.entry("col.coords", "Koordinater (X,Y)"),
                Map.entry("col.creation_date", "Oprettelsesdato"),
                Map.entry("col.engine_power", "Effekt"),
                Map.entry("col.distance", "Distance"),
                Map.entry("col.type", "Type"),
                Map.entry("col.fuel", "Brændstof"),
                Map.entry("col.owner", "Ejer"),
                Map.entry("col.price", "Pris"),
                Map.entry("filter.id", "ID"),
                Map.entry("filter.name", "Navn"),
                Map.entry("filter.owner", "Ejer"),
                Map.entry("filter.min_price", "Min. pris"),
                Map.entry("filter.max_price", "Maks. pris"),
                Map.entry("filter.type", "Type"),
                Map.entry("filter.fuel", "Brændstof"),

                // Добавленные ключи локализации
                Map.entry("table.refresh", "Opdater"),

                // Status og fejl
                Map.entry("app.status.initializing", "Initialiserer forbindelse..."),
                Map.entry("app.status.connecting", "Forbinder til server..."),
                Map.entry("app.status.loading_commands", "Indlæser kommandoer..."),
                Map.entry("app.status.ready", "Klar!"),
                Map.entry("app.status.connected", "Forbundet"),
                Map.entry("app.commands_loaded", "Kommandoer indlæst"),
                Map.entry("app.status.error", "Fejl"),
                Map.entry("app.status.disconnecting", "Lukker forbindelse..."),
                Map.entry("confirm.exit", "Er du sikker på, at du vil lukke?"),
                Map.entry("error.handshake", "Server svarede ikke på handshake"),
                Map.entry("error.commands_load", "Fejl ved indlæsning af kommandoer"),
                Map.entry("error.unknown", "Ukendt fejl"),

                Map.entry("auth.error.empty", "Brugernavn og adgangskode må ikke være tomme"),
                Map.entry("auth.error.auth", "Forkert brugernavn eller adgangskode"),
                Map.entry("auth.error.register", "Registreringsfejl"),
                Map.entry("main.user.label", "Bruger:"),
                Map.entry("main.table.title", "Objektliste"),
                Map.entry("main.visual.title", "Visualisering"),
                Map.entry("main.lang.label", "Sprog:"),
                Map.entry("table.filter", "Filter"),
                Map.entry("table.sort.asc", "Stigende"),
                Map.entry("table.sort.desc", "Faldende"),
                Map.entry("table.column.id", "ID"),
                Map.entry("table.column.name", "Navn"),
                Map.entry("table.column.x", "X"),
                Map.entry("table.column.y", "Y"),
                Map.entry("table.column.date", "Oprettelsesdato"),
                Map.entry("table.column.power", "Effekt"),
                Map.entry("table.column.distance", "Distance"),
                Map.entry("table.column.type", "Type"),
                Map.entry("table.column.fuel", "Brændstof"),
                Map.entry("table.column.price", "Pris"),
                Map.entry("table.column.owner", "Ejer"),
                Map.entry("vehicle.edit", "Rediger"),
                Map.entry("vehicle.delete", "Slet"),
                Map.entry("vehicle.buy", "Køb"),
                Map.entry("dialog.save", "Gem"),
                Map.entry("dialog.cancel", "Annuller"),
                Map.entry("error.network", "Netværksfejl"),
                Map.entry("error.server", "Serverfejl"),
                Map.entry("success.saved", "Gemt"),
                Map.entry("success.deleted", "Slettet"),
                Map.entry("confirm.delete", "Bekræft sletning af objekt")
        ));

        // EN_IN
        resources.put(EN_IN, Map.ofEntries(
                Map.entry("app.title", "Vehicle Manager"),
                Map.entry("auth.login", "Username"),
                Map.entry("auth.password", "Password"),
                Map.entry("auth.register", "Register"),

                Map.entry("vehicle.info", "Vehicle Info"),

                Map.entry("table.filter.all_types", "All Types"),
                Map.entry("table.filter.all_fuels", "All Fuels"),

                Map.entry("btn.show", "Show"),
                Map.entry("btn.add", "Add"),
                Map.entry("btn.update", "Update"),
                Map.entry("btn.remove", "Remove by ID"),
                Map.entry("btn.clear", "Clear"),
                Map.entry("btn.info", "Info"),
                Map.entry("btn.sort", "Sort"),
                Map.entry("btn.print_desc", "Print Descending"),
                Map.entry("btn.shuffle", "Shuffle"),
                Map.entry("btn.filter_engine", "Filter by Engine"),
                Map.entry("btn.buy", "Buy"),
                Map.entry("btn.balance", "Balance"),
                Map.entry("btn.deposit", "Deposit"),
                Map.entry("btn.help", "Help"),
                Map.entry("btn.exit", "Exit"),
                Map.entry("col.id", "ID"),
                Map.entry("col.name", "Name"),
                Map.entry("col.coords", "Coords (X,Y)"),
                Map.entry("col.creation_date", "Creation Date"),
                Map.entry("col.engine_power", "Engine Power"),
                Map.entry("col.distance", "Distance"),
                Map.entry("col.type", "Type"),
                Map.entry("col.fuel", "Fuel"),
                Map.entry("col.owner", "Owner"),
                Map.entry("col.price", "Price"),
                Map.entry("filter.id", "ID"),
                Map.entry("filter.name", "Name"),
                Map.entry("filter.owner", "Owner"),
                Map.entry("filter.min_price", "Min Price"),
                Map.entry("filter.max_price", "Max Price"),
                Map.entry("filter.type", "Type"),
                Map.entry("filter.fuel", "Fuel"),

                Map.entry("filter.all_types", "All Types"),
                Map.entry("filter.all_fuels", "All Fuels"),

                Map.entry("confirm.exit", "Are you sure you want to exit?"),


                Map.entry("auth.login.button", "Sign In"),
                Map.entry("auth.register.button", "Register"),
                Map.entry("auth.error.empty", "Username and password cannot be empty"),
                Map.entry("auth.error.auth", "Invalid username or password"),
                Map.entry("auth.error.register", "Registration error"),
                Map.entry("main.user.label", "User:"),
                Map.entry("main.table.title", "Objects List"),
                Map.entry("main.visual.title", "Visualization"),
                Map.entry("main.lang.label", "Language:"),
                Map.entry("table.filter", "Filter"),
                // Status and errors
                Map.entry("app.status.initializing", "Initializing connection..."),
                Map.entry("app.status.connecting", "Connecting to server..."),
                Map.entry("app.status.loading_commands", "Loading commands..."),
                Map.entry("app.status.ready", "Ready!"),
                Map.entry("app.status.connected", "Connected"),
                Map.entry("app.commands_loaded", "Commands loaded"),
                Map.entry("app.status.error", "Error"),
                Map.entry("app.status.disconnecting", "Closing connection..."),
                Map.entry("error.handshake", "Server did not respond to handshake"),
                Map.entry("error.commands_load", "Error loading command map"),
                Map.entry("error.unknown", "Unknown error"),

                Map.entry("table.sort.asc", "Ascending"),
                Map.entry("table.sort.desc", "Descending"),
                Map.entry("table.column.id", "ID"),
                Map.entry("table.column.name", "Name"),
                Map.entry("table.column.x", "X"),
                Map.entry("table.column.y", "Y"),
                Map.entry("table.column.date", "Created"),
                Map.entry("table.column.power", "Power"),
                Map.entry("table.column.distance", "Distance"),
                Map.entry("table.column.type", "Type"),
                Map.entry("table.column.fuel", "Fuel"),
                Map.entry("table.column.price", "Price"),
                Map.entry("table.column.owner", "Owner"),
                Map.entry("vehicle.edit", "Edit"),
                Map.entry("vehicle.delete", "Delete"),
                Map.entry("vehicle.buy", "Buy"),
                Map.entry("dialog.save", "Save"),
                Map.entry("dialog.cancel", "Cancel"),
                Map.entry("error.network", "Network error"),
                Map.entry("error.server", "Server error"),
                Map.entry("success.saved", "Saved"),
                Map.entry("success.deleted", "Deleted"),
                Map.entry("confirm.delete", "Confirm object deletion")
        ));
    }

    private void updateFormatters() {
        Locale loc = currentLocale.get();
        numberFormat = NumberFormat.getNumberInstance(loc);
        currencyFormat = NumberFormat.getCurrencyInstance(loc);
        dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(loc);
        dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(loc);
        timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(loc);
    }



    /** Получить строку по ключу для текущей локали */
    public String get(String key) {
        return resources.getOrDefault(currentLocale.get(), Collections.emptyMap())
                .getOrDefault(key, "???" + key + "???");
    }

    /** Получить строку для конкретной локали */
    public String get(String key, Locale locale) {
        return resources.getOrDefault(locale, Collections.emptyMap())
                .getOrDefault(key, "???" + key + "???");
    }

    /** Установить новую локаль (уведомляет всех слушателей) */
    public void setLocale(Locale locale) {
        if (SUPPORTED_LOCALES.contains(locale)) {
            currentLocale.set(locale);
        }
    }

    /** Получить наблюдаемую локаль (для привязки в UI) */
    public ObjectProperty<Locale> localeProperty() {
        return currentLocale;
    }

    /** Получить текущую локаль */
    public Locale getCurrentLocale() {
        return currentLocale.get();
    }


    /** Форматировать число согласно текущей локали */
    public String formatNumber(double value) {
        return numberFormat.format(value);
    }

    /** Форматировать валюту согласно текущей локали */
    public String formatCurrency(double value) {
        return currencyFormat.format(value);
    }

    /** Форматировать дату согласно текущей локали */
    public String formatDate(Date date) {
        if (date == null) return "";
        return dateFormatter.format(date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
    }

    /** Форматировать дату+время согласно текущей локали */
    public String formatDateTime(Date date) {
        if (date == null) return "";
        return dateTimeFormatter.format(date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
    }

    /** Форматировать время согласно текущей локали */
    public String formatTime(Date date) {
        if (date == null) return "";
        return timeFormatter.format(date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime());
    }

    /** Получить список доступных локалей для ComboBox */
    public List<Locale> getAvailableLocales() {
        return SUPPORTED_LOCALES;
    }

    /** Получить человекочитаемое название локали для отображения в UI */
    public String getLocaleDisplayName(Locale locale) {
        return switch (locale.toString()) {
            case "ru_RU" -> "Русский";
            case "nl_NL" -> "Nederlands";
            case "da_DK" -> "Dansk";
            case "en_IN" -> "English (India)";
            default -> locale.getDisplayName(locale);
        };
    }
}