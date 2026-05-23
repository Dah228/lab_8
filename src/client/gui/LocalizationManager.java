package client.gui;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * Менеджер локализации для поддержки 4 языков:
 * ru_RU, no_NO, lt_LT, en_GB
 *
 * Ресурсы хранятся ВНУТРИ класса (не во внешних файлах).
 * Поддерживает форматирование чисел, дат и времени.
 * Уведомляет об изменениях через ObjectProperty<Locale>.
 */
public class LocalizationManager {
    public static final Locale RU = new Locale("ru", "RU");
    public static final Locale NO = new Locale("no", "NO");      // Норвежский
    public static final Locale LT = new Locale("lt", "LT");      // Литовский
    public static final Locale EN_GB = new Locale("en", "GB");   // Английский (Великобритания)

    public static final List<Locale> SUPPORTED_LOCALES = List.of(RU, NO, LT, EN_GB);

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
        currentLocale.addListener((obs, oldLocale, newLocale) -> updateFormatters());
    }

    private void initializeResources() {
        // =====================================================================
        // RUSSIAN (ru_RU)
        // =====================================================================
        resources.put(RU, Map.ofEntries(
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
                Map.entry("main.visual.title", ">"),
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
                Map.entry("table.filter.all_types", "Все типы"),
                Map.entry("table.filter.all_fuels", "Все виды топлива"),
                Map.entry("filter.all_types", "Все типы"),
                Map.entry("filter.all_fuels", "Все топлива"),
                Map.entry("table.refresh", "Обновить"),

                // === НОВЫЕ КЛЮЧИ ДЛЯ ДИАЛОГОВ ===
                Map.entry("dialog.add_vehicle", "Добавление ТС"),
                Map.entry("dialog.edit_vehicle", "Редактирование ТС"),
                Map.entry("dialog.label.name", "Название:"),
                Map.entry("dialog.label.x", "X:"),
                Map.entry("dialog.label.y", "Y:"),
                Map.entry("dialog.label.power", "Мощность:"),
                Map.entry("dialog.label.distance", "Дистанция:"),
                Map.entry("dialog.label.type", "Тип:"),
                Map.entry("dialog.label.fuel", "Топливо:"),
                Map.entry("dialog.label.price", "Цена:"),
                Map.entry("dialog.prompt.name", "Название"),
                Map.entry("dialog.prompt.x", "X"),
                Map.entry("dialog.prompt.y", "Y"),
                Map.entry("dialog.prompt.power", "Мощность"),
                Map.entry("dialog.prompt.distance", "Дистанция"),
                Map.entry("dialog.prompt.price", "Цена"),
                Map.entry("dialog.remove_by_id.title", "Удаление по ID"),
                Map.entry("dialog.remove_by_id.prompt", "Введите ID:"),
                Map.entry("dialog.error.invalid_id", "Некорректный ID"),
                Map.entry("dialog.clear.title", "Очистка коллекции"),
                Map.entry("dialog.clear.confirm", "Вы уверены, что хотите удалить все свои объекты?"),
                Map.entry("dialog.update.title", "Обновление элемента"),
                Map.entry("dialog.update.prompt", "Введите ID:"),
                Map.entry("dialog.filter.power.title", "Фильтр по мощности двигателя"),
                Map.entry("dialog.filter.power.prompt", "Введите минимальную мощность:"),
                Map.entry("dialog.error.invalid_value", "Некорректное значение"),
                Map.entry("dialog.filter.type.title", "Фильтр по типу (меньше)"),
                Map.entry("dialog.group_by.title", "Группировка по полю"),
                Map.entry("dialog.buy.title", "Покупка ТС"),
                Map.entry("dialog.buy.prompt", "Введите ID транспортного средства:"),
                Map.entry("dialog.deposit.title", "Пополнение баланса"),
                Map.entry("dialog.deposit.prompt", "Введите сумму:"),
                Map.entry("dialog.error.positive_amount", "Сумма должна быть положительной"),
                Map.entry("dialog.error.invalid_amount", "Некорректная сумма"),
                Map.entry("dialog.set_price.title_id", "Установка цены"),
                Map.entry("dialog.set_price.prompt_id", "Введите ID:"),
                Map.entry("dialog.set_price.prompt_price", "Введите цену:"),
                Map.entry("dialog.error.negative_price", "Цена не может быть отрицательной"),
                Map.entry("dialog.error.invalid_price", "Некорректная цена"),
                Map.entry("dialog.result.title", "Результат выполнения"),
                Map.entry("dialog.error.unknown", "Произошла неизвестная ошибка"),
                Map.entry("dialog.error.invalid_data", "Некорректные данные: ")
        ));

        // =====================================================================
        // NORWEGIAN (no_NO)
        // =====================================================================
        resources.put(NO, Map.ofEntries(
                Map.entry("app.title", "Kjøretøybehandler"),
                Map.entry("confirm.exit", "Er du sikker på at du vil avslutte?"),
                Map.entry("auth.login", "Brukernavn"),
                Map.entry("auth.password", "Passord"),
                Map.entry("auth.register", "Registrer"),
                Map.entry("auth.login.button", "Logg inn"),
                Map.entry("auth.register.button", "Registrer deg"),
                Map.entry("auth.error.empty", "Brukernavn og passord kan ikke være tomme"),
                Map.entry("auth.error.auth", "Ugyldig brukernavn eller passord"),
                Map.entry("auth.error.register", "Registreringsfeil"),
                Map.entry("main.user.label", "Bruker:"),
                Map.entry("main.table.title", "Objektliste"),
                Map.entry("main.visual.title", "Visualisering"),
                Map.entry("main.lang.label", "Språk:"),
                Map.entry("table.filter", "Filter"),
                Map.entry("table.sort.asc", "Stigende"),
                Map.entry("table.sort.desc", "Synkende"),
                Map.entry("table.column.id", "ID"),
                Map.entry("table.column.name", "Navn"),
                Map.entry("table.column.x", "X"),
                Map.entry("table.column.y", "Y"),
                Map.entry("table.column.date", "Opprettelsesdato"),
                Map.entry("table.column.power", "Effekt"),
                Map.entry("table.column.distance", "Distanse"),
                Map.entry("table.column.type", "Type"),
                Map.entry("table.column.fuel", "Drivstoff"),
                Map.entry("table.column.price", "Pris"),
                Map.entry("table.column.owner", "Eier"),
                Map.entry("vehicle.edit", "Rediger"),
                Map.entry("vehicle.delete", "Slett"),
                Map.entry("vehicle.buy", "Kjøp"),
                Map.entry("dialog.save", "Lagre"),
                Map.entry("dialog.cancel", "Avbryt"),
                Map.entry("error.network", "Nettverksfeil"),
                Map.entry("error.server", "Serverfeil"),
                Map.entry("success.saved", "Lagret"),
                Map.entry("success.deleted", "Slettet"),
                Map.entry("confirm.delete", "Bekreft sletting av objekt"),
                Map.entry("app.status.initializing", "Initialiserer tilkobling..."),
                Map.entry("app.status.connecting", "Kobler til server..."),
                Map.entry("app.status.loading_commands", "Laster kommandoer..."),
                Map.entry("app.status.ready", "Klar!"),
                Map.entry("app.status.connected", "Tilkoblet"),
                Map.entry("app.commands_loaded", "Kommandoer lastet"),
                Map.entry("app.status.error", "Feil"),
                Map.entry("app.status.disconnecting", "Lukker tilkobling..."),
                Map.entry("error.handshake", "Server svarte ikke på håndtrykk"),
                Map.entry("error.commands_load", "Feil ved lasting av kommandokart"),
                Map.entry("error.unknown", "Ukjent feil"),
                Map.entry("vehicle.info", "Objektinformasjon"),
                Map.entry("btn.show", "Vis"),
                Map.entry("btn.add", "Legg til"),
                Map.entry("btn.update", "Oppdater"),
                Map.entry("btn.remove", "Fjern etter ID"),
                Map.entry("btn.clear", "Tøm"),
                Map.entry("btn.info", "Info"),
                Map.entry("btn.sort", "Sorter"),
                Map.entry("btn.print_desc", "Skriv ut synkende"),
                Map.entry("btn.shuffle", "Bland"),
                Map.entry("btn.filter_engine", "Filtrer etter effekt"),
                Map.entry("btn.buy", "Kjøp"),
                Map.entry("btn.balance", "Saldo"),
                Map.entry("btn.deposit", "Sett inn"),
                Map.entry("btn.help", "Hjelp"),
                Map.entry("btn.exit", "Avslutt"),
                Map.entry("col.id", "ID"),
                Map.entry("col.name", "Navn"),
                Map.entry("col.coords", "Koordinater (X,Y)"),
                Map.entry("col.creation_date", "Opprettelsesdato"),
                Map.entry("col.engine_power", "Effekt"),
                Map.entry("col.distance", "Distanse"),
                Map.entry("col.type", "Type"),
                Map.entry("col.fuel", "Drivstoff"),
                Map.entry("col.owner", "Eier"),
                Map.entry("col.price", "Pris"),
                Map.entry("filter.id", "ID"),
                Map.entry("filter.name", "Navn"),
                Map.entry("filter.owner", "Eier"),
                Map.entry("filter.min_price", "Min. pris"),
                Map.entry("filter.max_price", "Maks. pris"),
                Map.entry("filter.type", "Type"),
                Map.entry("filter.fuel", "Drivstoff"),
                Map.entry("table.filter.all_types", "Alle typer"),
                Map.entry("table.filter.all_fuels", "Alle drivstoff"),
                Map.entry("filter.all_types", "Alle typer"),
                Map.entry("filter.all_fuels", "Alle drivstoff"),
                Map.entry("table.refresh", "Oppdater"),

                // === NEW DIALOG KEYS ===
                Map.entry("dialog.add_vehicle", "Legg til kjøretøy"),
                Map.entry("dialog.edit_vehicle", "Rediger kjøretøy"),
                Map.entry("dialog.label.name", "Navn:"),
                Map.entry("dialog.label.x", "X:"),
                Map.entry("dialog.label.y", "Y:"),
                Map.entry("dialog.label.power", "Effekt:"),
                Map.entry("dialog.label.distance", "Distanse:"),
                Map.entry("dialog.label.type", "Type:"),
                Map.entry("dialog.label.fuel", "Drivstoff:"),
                Map.entry("dialog.label.price", "Pris:"),
                Map.entry("dialog.prompt.name", "Navn"),
                Map.entry("dialog.prompt.x", "X"),
                Map.entry("dialog.prompt.y", "Y"),
                Map.entry("dialog.prompt.power", "Effekt"),
                Map.entry("dialog.prompt.distance", "Distanse"),
                Map.entry("dialog.prompt.price", "Pris"),
                Map.entry("dialog.remove_by_id.title", "Fjern etter ID"),
                Map.entry("dialog.remove_by_id.prompt", "Skriv inn ID:"),
                Map.entry("dialog.error.invalid_id", "Ugyldig ID"),
                Map.entry("dialog.clear.title", "Tøm samling"),
                Map.entry("dialog.clear.confirm", "Er du sikker på at du vil slette alle objektene dine?"),
                Map.entry("dialog.update.title", "Oppdater element"),
                Map.entry("dialog.update.prompt", "Skriv inn ID:"),
                Map.entry("dialog.filter.power.title", "Filtrer etter motoreffekt"),
                Map.entry("dialog.filter.power.prompt", "Skriv inn minimum effekt:"),
                Map.entry("dialog.error.invalid_value", "Ugyldig verdi"),
                Map.entry("dialog.filter.type.title", "Filtrer etter type (mindre)"),
                Map.entry("dialog.group_by.title", "Grupper etter felt"),
                Map.entry("dialog.buy.title", "Kjøp kjøretøy"),
                Map.entry("dialog.buy.prompt", "Skriv inn kjøretøy-ID:"),
                Map.entry("dialog.deposit.title", "Sett inn saldo"),
                Map.entry("dialog.deposit.prompt", "Skriv inn beløp:"),
                Map.entry("dialog.error.positive_amount", "Beløpet må være positivt"),
                Map.entry("dialog.error.invalid_amount", "Ugyldig beløp"),
                Map.entry("dialog.set_price.title_id", "Sett pris"),
                Map.entry("dialog.set_price.prompt_id", "Skriv inn ID:"),
                Map.entry("dialog.set_price.prompt_price", "Skriv inn pris:"),
                Map.entry("dialog.error.negative_price", "Pris kan ikke være negativ"),
                Map.entry("dialog.error.invalid_price", "Ugyldig pris"),
                Map.entry("dialog.result.title", "Resultat"),
                Map.entry("dialog.error.unknown", "En ukjent feil oppstod"),
                Map.entry("dialog.error.invalid_data", "Ugyldige data: ")
        ));

        // =====================================================================
        // LITHUANIAN (lt_LT)
        // =====================================================================
        resources.put(LT, Map.ofEntries(
                Map.entry("app.title", "Transporto priemonių valdytojas"),
                Map.entry("confirm.exit", "Ar tikrai norite išeiti?"),
                Map.entry("auth.login", "Prisijungimo vardas"),
                Map.entry("auth.password", "Slaptažodis"),
                Map.entry("auth.register", "Registruotis"),
                Map.entry("auth.login.button", "Prisijungti"),
                Map.entry("auth.register.button", "Registruotis"),
                Map.entry("auth.error.empty", "Prisijungimo vardas ir slaptažodis negali būti tušti"),
                Map.entry("auth.error.auth", "Neteisingas prisijungimo vardas arba slaptažodis"),
                Map.entry("auth.error.register", "Registracijos klaida"),
                Map.entry("main.user.label", "Naudotojas:"),
                Map.entry("main.table.title", "Objektų sąrašas"),
                Map.entry("main.visual.title", "Vizualizacija"),
                Map.entry("main.lang.label", "Kalba:"),
                Map.entry("table.filter", "Filtruoti"),
                Map.entry("table.sort.asc", "Didėjančia tvarka"),
                Map.entry("table.sort.desc", "Mažėjančia tvarka"),
                Map.entry("table.column.id", "ID"),
                Map.entry("table.column.name", "Pavadinimas"),
                Map.entry("table.column.x", "X"),
                Map.entry("table.column.y", "Y"),
                Map.entry("table.column.date", "Sukūrimo data"),
                Map.entry("table.column.power", "Galia"),
                Map.entry("table.column.distance", "Atstumas"),
                Map.entry("table.column.type", "Tipas"),
                Map.entry("table.column.fuel", "Kuras"),
                Map.entry("table.column.price", "Kaina"),
                Map.entry("table.column.owner", "Savininkas"),
                Map.entry("vehicle.edit", "Redaguoti"),
                Map.entry("vehicle.delete", "Ištrinti"),
                Map.entry("vehicle.buy", "Pirkti"),
                Map.entry("dialog.save", "Išsaugoti"),
                Map.entry("dialog.cancel", "Atšaukti"),
                Map.entry("error.network", "Tinklo klaida"),
                Map.entry("error.server", "Serverio klaida"),
                Map.entry("success.saved", "Išsaugota"),
                Map.entry("success.deleted", "Ištrinta"),
                Map.entry("confirm.delete", "Patvirtinkite objekto ištrynimą"),
                Map.entry("app.status.initializing", "Inicijuojamas ryšys..."),
                Map.entry("app.status.connecting", "Jungiamasi prie serverio..."),
                Map.entry("app.status.loading_commands", "Kraunamos komandos..."),
                Map.entry("app.status.ready", "Paruošta!"),
                Map.entry("app.status.connected", "Prisijungta"),
                Map.entry("app.commands_loaded", "Komandos įkeltos"),
                Map.entry("app.status.error", "Klaida"),
                Map.entry("app.status.disconnecting", "Uždaromas ryšys..."),
                Map.entry("error.handshake", "Serveris neatsakė į rankos paspaudimą"),
                Map.entry("error.commands_load", "Klaida kraunant komandų žemėlapį"),
                Map.entry("error.unknown", "Nežinoma klaida"),
                Map.entry("vehicle.info", "Informacija apie objektą"),
                Map.entry("btn.show", "Rodyti"),
                Map.entry("btn.add", "Pridėti"),
                Map.entry("btn.update", "Atnaujinti"),
                Map.entry("btn.remove", "Šalinti pagal ID"),
                Map.entry("btn.clear", "Išvalyti"),
                Map.entry("btn.info", "Info"),
                Map.entry("btn.sort", "Rūšiuoti"),
                Map.entry("btn.print_desc", "Spausdinti mažėjančia tvarka"),
                Map.entry("btn.shuffle", "Sumaišyti"),
                Map.entry("btn.filter_engine", "Filtruoti pagal galią"),
                Map.entry("btn.buy", "Pirkti"),
                Map.entry("btn.balance", "Balansas"),
                Map.entry("btn.deposit", "Įnešti"),
                Map.entry("btn.help", "Pagalba"),
                Map.entry("btn.exit", "Išeiti"),
                Map.entry("col.id", "ID"),
                Map.entry("col.name", "Pavadinimas"),
                Map.entry("col.coords", "Koordinatės (X,Y)"),
                Map.entry("col.creation_date", "Sukūrimo data"),
                Map.entry("col.engine_power", "Variklio galia"),
                Map.entry("col.distance", "Nuvažiuotas atstumas"),
                Map.entry("col.type", "Tipas"),
                Map.entry("col.fuel", "Kuras"),
                Map.entry("col.owner", "Savininkas"),
                Map.entry("col.price", "Kaina"),
                Map.entry("filter.id", "ID"),
                Map.entry("filter.name", "Pavadinimas"),
                Map.entry("filter.owner", "Savininkas"),
                Map.entry("filter.min_price", "Min. kaina"),
                Map.entry("filter.max_price", "Maks. kaina"),
                Map.entry("filter.type", "Tipas"),
                Map.entry("filter.fuel", "Kuras"),
                Map.entry("table.filter.all_types", "Visi tipai"),
                Map.entry("table.filter.all_fuels", "Visi kuro tipai"),
                Map.entry("filter.all_types", "Visi tipai"),
                Map.entry("filter.all_fuels", "Visi kuro tipai"),
                Map.entry("table.refresh", "Atnaujinti"),

                // === NEW DIALOG KEYS ===
                Map.entry("dialog.add_vehicle", "Pridėti transporto priemonę"),
                Map.entry("dialog.edit_vehicle", "Redaguoti transporto priemonę"),
                Map.entry("dialog.label.name", "Pavadinimas:"),
                Map.entry("dialog.label.x", "X:"),
                Map.entry("dialog.label.y", "Y:"),
                Map.entry("dialog.label.power", "Galia:"),
                Map.entry("dialog.label.distance", "Atstumas:"),
                Map.entry("dialog.label.type", "Tipas:"),
                Map.entry("dialog.label.fuel", "Kuras:"),
                Map.entry("dialog.label.price", "Kaina:"),
                Map.entry("dialog.prompt.name", "Pavadinimas"),
                Map.entry("dialog.prompt.x", "X"),
                Map.entry("dialog.prompt.y", "Y"),
                Map.entry("dialog.prompt.power", "Galia"),
                Map.entry("dialog.prompt.distance", "Atstumas"),
                Map.entry("dialog.prompt.price", "Kaina"),
                Map.entry("dialog.remove_by_id.title", "Šalinti pagal ID"),
                Map.entry("dialog.remove_by_id.prompt", "Įveskite ID:"),
                Map.entry("dialog.error.invalid_id", "Neteisingas ID"),
                Map.entry("dialog.clear.title", "Išvalyti kolekciją"),
                Map.entry("dialog.clear.confirm", "Ar tikrai norite ištrinti visus savo objektus?"),
                Map.entry("dialog.update.title", "Atnaujinti elementą"),
                Map.entry("dialog.update.prompt", "Įveskite ID:"),
                Map.entry("dialog.filter.power.title", "Filtruoti pagal variklio galią"),
                Map.entry("dialog.filter.power.prompt", "Įveskite minimalią galią:"),
                Map.entry("dialog.error.invalid_value", "Neteisinga reikšmė"),
                Map.entry("dialog.filter.type.title", "Filtruoti pagal tipą (mažiau)"),
                Map.entry("dialog.group_by.title", "Grupuoti pagal lauką"),
                Map.entry("dialog.buy.title", "Pirkti transporto priemonę"),
                Map.entry("dialog.buy.prompt", "Įveskite transporto priemonės ID:"),
                Map.entry("dialog.deposit.title", "Įnešti į sąskaitą"),
                Map.entry("dialog.deposit.prompt", "Įveskite sumą:"),
                Map.entry("dialog.error.positive_amount", "Suma turi būti teigiama"),
                Map.entry("dialog.error.invalid_amount", "Neteisinga suma"),
                Map.entry("dialog.set_price.title_id", "Nustatyti kainą"),
                Map.entry("dialog.set_price.prompt_id", "Įveskite ID:"),
                Map.entry("dialog.set_price.prompt_price", "Įveskite kainą:"),
                Map.entry("dialog.error.negative_price", "Kaina negali būti neigiama"),
                Map.entry("dialog.error.invalid_price", "Neteisinga kaina"),
                Map.entry("dialog.result.title", "Rezultatas"),
                Map.entry("dialog.error.unknown", "Įvyko nežinoma klaida"),
                Map.entry("dialog.error.invalid_data", "Neteisingi duomenys: ")
        ));

        // =====================================================================
        // ENGLISH - UNITED KINGDOM (en_GB)
        // =====================================================================
        resources.put(EN_GB, Map.ofEntries(
                Map.entry("app.title", "Vehicle Manager"),
                Map.entry("confirm.exit", "Are you sure you want to exit?"),
                Map.entry("auth.login", "Username"),
                Map.entry("auth.password", "Password"),
                Map.entry("auth.register", "Register"),
                Map.entry("auth.login.button", "Sign In"),
                Map.entry("auth.register.button", "Register"),
                Map.entry("auth.error.empty", "Username and password cannot be empty"),
                Map.entry("auth.error.auth", "Invalid username or password"),
                Map.entry("auth.error.register", "Registration error"),
                Map.entry("main.user.label", "User:"),
                Map.entry("main.table.title", "Objects List"),
                Map.entry("main.visual.title", ">"),
                Map.entry("main.lang.label", "Language:"),
                Map.entry("table.filter", "Filter"),
                Map.entry("table.sort.asc", "Ascending"),
                Map.entry("table.sort.desc", "Descending"),
                Map.entry("table.column.id", "ID"),
                Map.entry("table.column.name", "Name"),
                Map.entry("table.column.x", "X"),
                Map.entry("table.column.y", "Y"),
                Map.entry("table.column.date", "Creation Date"),
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
                Map.entry("confirm.delete", "Confirm object deletion"),
                Map.entry("app.status.initializing", "Initialising connection..."),
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
                Map.entry("vehicle.info", "Vehicle Information"),
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
                Map.entry("col.distance", "Distance Travelled"),
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
                Map.entry("table.filter.all_types", "All Types"),
                Map.entry("table.filter.all_fuels", "All Fuels"),
                Map.entry("filter.all_types", "All Types"),
                Map.entry("filter.all_fuels", "All Fuels"),
                Map.entry("table.refresh", "Refresh"),

                // === NEW DIALOG KEYS ===
                Map.entry("dialog.add_vehicle", "Add Vehicle"),
                Map.entry("dialog.edit_vehicle", "Edit Vehicle"),
                Map.entry("dialog.label.name", "Name:"),
                Map.entry("dialog.label.x", "X:"),
                Map.entry("dialog.label.y", "Y:"),
                Map.entry("dialog.label.power", "Power:"),
                Map.entry("dialog.label.distance", "Distance:"),
                Map.entry("dialog.label.type", "Type:"),
                Map.entry("dialog.label.fuel", "Fuel:"),
                Map.entry("dialog.label.price", "Price:"),
                Map.entry("dialog.prompt.name", "Name"),
                Map.entry("dialog.prompt.x", "X"),
                Map.entry("dialog.prompt.y", "Y"),
                Map.entry("dialog.prompt.power", "Power"),
                Map.entry("dialog.prompt.distance", "Distance"),
                Map.entry("dialog.prompt.price", "Price"),
                Map.entry("dialog.remove_by_id.title", "Remove by ID"),
                Map.entry("dialog.remove_by_id.prompt", "Enter ID:"),
                Map.entry("dialog.error.invalid_id", "Invalid ID"),
                Map.entry("dialog.clear.title", "Clear Collection"),
                Map.entry("dialog.clear.confirm", "Are you sure you want to delete all your objects?"),
                Map.entry("dialog.update.title", "Update Element"),
                Map.entry("dialog.update.prompt", "Enter ID:"),
                Map.entry("dialog.filter.power.title", "Filter by Engine Power"),
                Map.entry("dialog.filter.power.prompt", "Enter minimum power:"),
                Map.entry("dialog.error.invalid_value", "Invalid value"),
                Map.entry("dialog.filter.type.title", "Filter by Type (Less Than)"),
                Map.entry("dialog.group_by.title", "Group by Field"),
                Map.entry("dialog.buy.title", "Buy Vehicle"),
                Map.entry("dialog.buy.prompt", "Enter vehicle ID:"),
                Map.entry("dialog.deposit.title", "Deposit Balance"),
                Map.entry("dialog.deposit.prompt", "Enter amount:"),
                Map.entry("dialog.error.positive_amount", "Amount must be positive"),
                Map.entry("dialog.error.invalid_amount", "Invalid amount"),
                Map.entry("dialog.set_price.title_id", "Set Price"),
                Map.entry("dialog.set_price.prompt_id", "Enter ID:"),
                Map.entry("dialog.set_price.prompt_price", "Enter price:"),
                Map.entry("dialog.error.negative_price", "Price cannot be negative"),
                Map.entry("dialog.error.invalid_price", "Invalid price"),
                Map.entry("dialog.result.title", "Result"),
                Map.entry("dialog.error.unknown", "An unknown error occurred"),
                Map.entry("dialog.error.invalid_data", "Invalid data: ")
        ));
    }

    private void updateFormatters() {
        Locale loc = currentLocale.get();
        numberFormat = NumberFormat.getNumberInstance(loc);
        currencyFormat = NumberFormat.getCurrencyInstance(loc);
        dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(loc);

        dateTimeFormatter = DateTimeFormatter.ofPattern(getDateTimePattern()).withLocale(loc);

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
            case "no_NO" -> "Norsk";
            case "lt_LT" -> "Lietuvių";
            case "en_GB" -> "English (UK)";
            default -> locale.getDisplayName(locale);
        };
    }

    private String getDateTimePattern() {
        return switch (currentLocale.get().toString()) {
            case "ru_RU" -> "dd.MM.yyyy HH:mm";      // 23.05.2026 14:30
            case "no_NO" -> "dd.MM.yyyy HH:mm";      // 23.05.2026 14:30
            case "lt_LT" -> "yyyy-MM-dd HH:mm";      // 2026-05-23 14:30
            case "en_GB" -> "dd/MM/yyyy HH:mm";      // 23/05/2026 14:30
            default      -> "dd.MM.yyyy HH:mm";
        };
    }
}