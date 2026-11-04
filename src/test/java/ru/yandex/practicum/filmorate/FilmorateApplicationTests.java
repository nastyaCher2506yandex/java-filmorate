package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RequiredArgsConstructor
class FilmorateApplicationTests {

	@Autowired
	FilmService filmService;
	@Autowired
	UserService userService;

	@Autowired
	private TestRestTemplate restTemplate;

	HttpHeaders headers;

	@Test
	void contextLoads() {
	}



	@BeforeEach
	void setUpSetting() {
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
	}

	//проверка контроллеров на пустые запросы и корректные запросы
	@Test
	void server_reacts_to_empty_request_with_500_film() {
		// Отправляем пустой JSON {}
		ResponseEntity<String> response = restTemplate.postForEntity(
				"/films",
				new HttpEntity<>("{}", headers),
				String.class
		);

		// Проверяем, что сервер реально вернул статус 500
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}

	@Test
	void server_reacts_to_valid_request_with_200_film() {
		String validJson = "{\"name\":\"Test Film\",\"description\":\"Good description\",\"releaseDate\":\"1995-12-27\",\"duration\":120}";

		ResponseEntity<String> response = restTemplate.postForEntity(
				"/films",
				new HttpEntity<>(validJson, headers),
				String.class
		);

		// Проверяем, что сервер реально вернул статус 200
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	void server_reacts_to_empty_request_with_500_user() {
		// Отправляем пустой JSON {}
		ResponseEntity<String> response = restTemplate.postForEntity(
				"/users",
				new HttpEntity<>("{}", headers),
				String.class
		);

		// Проверяем, что сервер реально вернул статус 500
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
	}

	@Test
	void server_reacts_to_valid_request_with_200_user() {
		String validJson = "{\"login\":\"dolore\",\"name\":\"Nick Name\",\"email\":\"mail@mail.ru\",\"birthday\":\"1946-08-20\"}";

		ResponseEntity<String> response = restTemplate.postForEntity(
				"/users",
				new HttpEntity<>(validJson, headers),
				String.class
		);

		// Проверяем, что сервер реально вернул статус 200
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	//проверим работу аннотацию для email @Email и @Valid
	@Test
	void server_reacts_to_incorrect_emai_with_400_user() {
		String incorrectEmail = "{\"login\":\"dolore\",\"name\":\"Nick Name\",\"email\":\"это-неправильный?эмейл@\",\"birthday\":\"1946-08-20\"}";

		ResponseEntity<String> response = restTemplate.postForEntity(
				"/users",
				new HttpEntity<>(incorrectEmail, headers),
				String.class
		);

		// Проверяем, что сервер реально вернул статус 200
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

		// Проверяем, что сервер реально вернул статус 400, так @Valid автоматически возращает
		//ошибку некорректных данных
		incorrectEmail = "{\"login\":\"dolore\",\"name\":\"Nick Name\",\"email\":\"это-неправильный?эмейл\",\"birthday\":\"1946-08-20\"}";

		ResponseEntity<String> response1 = restTemplate.postForEntity(
				"/users",
				new HttpEntity<>(incorrectEmail, headers),
				String.class
		);

		// Проверяем, что сервер реально вернул статус 400, так @Valid автоматически возращает
		//ошибку некорректных данных
		assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());
	}

	//теперь сделаем проверку самих сервисов
	//ФИЛЬМЫ

	//проверяем название фильма
	@Test
	void service_reacts_to_incorrect_name_film() {
		//когда название пустое
		Film emptyName = new Film();
		emptyName.setDescription("Описание фильма длиной меньше 200 символов");
		emptyName.setReleaseDate(LocalDate.of(2020, 1, 1));
		emptyName.setDuration(120);

		//проверяем, что ошибка поймана
		assertThrows(ValidationException.class,() -> filmService.create(emptyName));

		Film nameOnlySpace = new Film();
		nameOnlySpace.setName("     ");
		nameOnlySpace.setDescription("Описание фильма длиной меньше 200 символов");
		nameOnlySpace.setReleaseDate(LocalDate.of(2020, 1, 1));
		nameOnlySpace.setDuration(120);

		//проверяем, что ошибка поймана
		assertThrows(ValidationException.class,() -> filmService.create(nameOnlySpace));
	}

	//проверяем длину описания
	@Test
	void service_reacts_to_incorrect_and_correct_description_film() {
		//когда название пустое
		Film emptyDescription = new Film();
		emptyDescription.setName("Test Name");
		emptyDescription.setReleaseDate(LocalDate.of(2020, 1, 1));
		emptyDescription.setDuration(120);

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			Film result = filmService.create(emptyDescription);
			assertTrue(result.getDescription().isEmpty());
		});

		//когда содержит количество символов диапозоне

		Film descriptionCorrect = new Film();
		descriptionCorrect.setName("Test Name");
		descriptionCorrect.setDescription("Описание фильма длиной меньше 200 символов");
		descriptionCorrect.setReleaseDate(LocalDate.of(2020, 1, 1));
		descriptionCorrect.setDuration(120);

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			Film result = filmService.create(descriptionCorrect);
			assertEquals("Описание фильма длиной меньше 200 символов", result.getDescription());
		});

		//когда содержит ровно 200 символов
		Film descriptionUnderMaxChar = new Film();
		descriptionUnderMaxChar.setName("Test Name");
		descriptionUnderMaxChar.setDescription("В заброшенной обсерватории астроном замечает странную аномалию: " +
				"звёзды гаснут в чётком порядке. Приборы один за другим выходят из строя а в эфире звучит " +
				"чужой голос — будто из иной, далёкой реальности");
		descriptionUnderMaxChar.setReleaseDate(LocalDate.of(2020, 1, 1));
		descriptionUnderMaxChar.setDuration(120);

		assertEquals(200,descriptionUnderMaxChar.getDescription().length());

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			Film result = filmService.create(descriptionUnderMaxChar);
			assertEquals(200, result.getDescription().length());
		});

		//когда содержит 201 символ
		Film descriptionMoreOneChar = new Film();
		descriptionMoreOneChar.setName("Test Name");
		descriptionMoreOneChar.setDescription("В заброшенной обсерватории астроном замечает странную аномалию: " +
				"звёзды гаснут в чётком порядке. Приборы один за другим выходят из строя, а в эфире звучит " +
				"чужой голос — будто из иной, далёкой реальности");
		descriptionMoreOneChar.setReleaseDate(LocalDate.of(2020, 1, 1));
		descriptionMoreOneChar.setDuration(120);

		assertEquals(201,descriptionMoreOneChar.getDescription().length());

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> filmService.create(descriptionMoreOneChar));

		//когда содержит 250 символов
		Film descriptionMore = new Film();
		descriptionMore.setName("Test Name");
		descriptionMore.setDescription("Астроном смотрит в окуляр. Звёзды гаснут по порядку. " +
				"Приборы один за другим щёлкают и гаснут. В эфире — чужой голос, будто из другой вселенной. " +
				"Пот стекает по шее, но он не отрывается от линзы. Тишина давит, экраны мерцают. " +
				"Холод пробирает до костей.");
		descriptionMore.setReleaseDate(LocalDate.of(2020, 1, 1));
		descriptionMore.setDuration(120);

		assertEquals(250,descriptionMore.getDescription().length());


		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> filmService.create(descriptionMore));
	}

	//проверяем дату реализации
	@Test
	void service_reacts_to_incorrect_and_correct_data_release_film() {
		//когда не указано
		Film filmWhenDataReleaseEmpty = new Film();
		filmWhenDataReleaseEmpty.setName("Test Name");
		filmWhenDataReleaseEmpty.setDescription("Описание фильма длиной меньше 200 символов");
		filmWhenDataReleaseEmpty.setDuration(120);

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> filmService.create(filmWhenDataReleaseEmpty));

		//когда дата позже 28 декабря 1895 года
		Film filmWhenDataReleaseAfter = new Film();
		filmWhenDataReleaseAfter.setName("Test Name");
		filmWhenDataReleaseAfter.setDescription("Описание фильма длиной меньше 200 символов");
		filmWhenDataReleaseAfter.setReleaseDate(LocalDate.of(2020, 1, 1));
		filmWhenDataReleaseAfter.setDuration(120);

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			Film result = filmService.create(filmWhenDataReleaseAfter);
			assertEquals(LocalDate.of(2020, 1, 1),result.getReleaseDate());
		});

		//когда ровно 28 декабря 1895 года
		Film filmWhenDataReleaseNow = new Film();
		filmWhenDataReleaseNow.setName("Test Name");
		filmWhenDataReleaseNow.setDescription("Описание фильма длиной меньше 200 символов");
		filmWhenDataReleaseNow.setReleaseDate(LocalDate.of(1895, 12, 28));
		filmWhenDataReleaseNow.setDuration(120);

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			Film result = filmService.create(filmWhenDataReleaseNow);

			assertEquals(LocalDate.of(1895, 12, 28),result.getReleaseDate());
		});

		//когда раньше на один день
		Film filmWhenDataReleaseBeforeOneDay = new Film();
		filmWhenDataReleaseBeforeOneDay.setName("Test Name");
		filmWhenDataReleaseBeforeOneDay.setDescription("Описание фильма длиной меньше 200 символов");
		filmWhenDataReleaseBeforeOneDay.setReleaseDate(LocalDate.of(1895, 12, 27));
		filmWhenDataReleaseBeforeOneDay.setDuration(120);

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> filmService.create(filmWhenDataReleaseBeforeOneDay));

		//когда намного раньше
		Film filmWhenDataReleaseBeforeMore = new Film();
		filmWhenDataReleaseBeforeMore.setName("Test Name");
		filmWhenDataReleaseBeforeMore.setDescription("Описание фильма длиной меньше 200 символов");
		filmWhenDataReleaseBeforeMore.setReleaseDate(LocalDate.of(1800, 12, 27));
		filmWhenDataReleaseBeforeMore.setDuration(120);

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> filmService.create(filmWhenDataReleaseBeforeMore));
	}

	//проверим продолжительность фильмов
	@Test
	void service_reacts_to_incorrect_and_correct_duraction_film() {
		//когда продолжительность не передана
		Film filmWhenEmptyDuration = new Film();
		filmWhenEmptyDuration.setName("Test Name");
		filmWhenEmptyDuration.setDescription("Описание фильма длиной меньше 200 символов");
		filmWhenEmptyDuration.setReleaseDate(LocalDate.of(2020, 1, 1));

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> filmService.create(filmWhenEmptyDuration));

		//продолжительность правильная
		Film filmWhenCorrectDuration = new Film();
		filmWhenCorrectDuration.setName("Test Name");
		filmWhenCorrectDuration.setDescription("Описание фильма длиной меньше 200 символов");
		filmWhenCorrectDuration.setReleaseDate(LocalDate.of(2020, 1, 1));
		filmWhenCorrectDuration.setDuration(120);

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			Film result = filmService.create(filmWhenCorrectDuration);
			assertEquals(120,result.getDuration());
		});

		//когда равно нулю
		Film filmWhenZeroDuration = new Film();
		filmWhenZeroDuration.setName("Test Name");
		filmWhenZeroDuration.setDescription("Описание фильма длиной меньше 200 символов");
		filmWhenZeroDuration.setReleaseDate(LocalDate.of(2020, 1, 1));
		filmWhenZeroDuration.setDuration(0);

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> filmService.create(filmWhenZeroDuration));

		//когда продолжительность отрицательная
		Film filmWhenNegativeDuration = new Film();
		filmWhenNegativeDuration.setName("Test Name");
		filmWhenNegativeDuration.setDescription("Описание фильма длиной меньше 200 символов");
		filmWhenNegativeDuration.setReleaseDate(LocalDate.of(2020, 1, 1));
		filmWhenNegativeDuration.setDuration(-200);

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> filmService.create(filmWhenNegativeDuration));
	}

	//ПОЛЬЗОВАТЕЛИ
	//проверяем электронную почту
	@Test
	void service_reacts_to_incorrect_and_correct_email_user() {
		//когда не передали почту
		User userEmptyEmail = new User();
		userEmptyEmail.setBirthday(LocalDate.of(2020, 1, 1));
		userEmptyEmail.setLogin("testlogin");
		userEmptyEmail.setName("testname");

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> userService.create(userEmptyEmail));

		//когда нет символа @
		User userWithoutCharEmail = new User();
		userWithoutCharEmail.setBirthday(LocalDate.of(2020, 1, 1));
		userWithoutCharEmail.setLogin("testlogin");
		userWithoutCharEmail.setName("testname");
		userWithoutCharEmail.setEmail("testmail.com");

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> userService.create(userWithoutCharEmail));

		//правильная почта
		User userCorrectEmail = new User();
		userCorrectEmail.setBirthday(LocalDate.of(2020, 1, 1));
		userCorrectEmail.setLogin("testlogin");
		userCorrectEmail.setName("testname");
		userCorrectEmail.setEmail("test@mail.com");

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			User result = userService.create(userCorrectEmail);
			assertEquals("test@mail.com",result.getEmail());
		});

		//когда такая почта уже существует
		User userDuplicateEmail = new User();
		userDuplicateEmail.setBirthday(LocalDate.of(2020, 1, 1));
		userDuplicateEmail.setLogin("testlogin2");
		userDuplicateEmail.setName("testname2");
		userDuplicateEmail.setEmail("test@mail.com");

		//проверяем, что ошибка была поймана
		assertThrows(DuplicatedDataException.class,() -> userService.create(userDuplicateEmail));
	}

	//проверяем логин
	@Test
	void service_reacts_to_incorrect_and_correct_login_user() {
		//когда не передан логин
		User userEmptyLogin = new User();
		userEmptyLogin.setBirthday(LocalDate.of(2020, 1, 1));
		userEmptyLogin.setName("testname2");
		userEmptyLogin.setEmail("test2@mail.com");

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> userService.create(userEmptyLogin));

		//когда логин содержит пробелы
		User userWithSpaceLogin = new User();
		userWithSpaceLogin.setLogin("test  login2   ");
		userWithSpaceLogin.setBirthday(LocalDate.of(2020, 1, 1));
		userWithSpaceLogin.setName("testname2");
		userWithSpaceLogin.setEmail("test2@mail.com");

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> userService.create(userWithSpaceLogin));
	}

	//проверяем имя
	@Test
	void service_reacts_to_name_user() {
		//когда имя пользователя передано
		User userWithName = new User();
		userWithName.setBirthday(LocalDate.of(2020, 1, 1));
		userWithName.setLogin("testlogin");
		userWithName.setName("testname");
		userWithName.setEmail("test3@mail.com");

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			User result = userService.create(userWithName);
			assertEquals("testname",result.getName());
		});

		//когда имя пользователя не передано
		User userWithoutName = new User();
		userWithoutName.setBirthday(LocalDate.of(2020, 1, 1));
		userWithoutName.setLogin("testlogin");
		userWithoutName.setEmail("test7@mail.com");

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			User result = userService.create(userWithoutName);
			assertEquals("testlogin",result.getName());
		});
	}

	//проверяем дату рождения пользователя
	@Test
	void service_reacts_to_correct_incorrect_birthday_user() {
		//когда дата рождения намного раньше будущего
		User userCorrectBirthday = new User();
		userCorrectBirthday.setBirthday(LocalDate.of(2020, 1, 1));
		userCorrectBirthday.setLogin("testlogin");
		userCorrectBirthday.setName("testname");
		userCorrectBirthday.setEmail("test56@mail.com");

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			User result = userService.create(userCorrectBirthday);
			assertEquals(LocalDate.of(2020, 1, 1),result.getBirthday());
		});

		//когда день рождения на один день раньше будущего
		User userCorrectBirthdayOneBefore = new User();
		userCorrectBirthdayOneBefore.setBirthday(LocalDate.now().minusDays(1));
		userCorrectBirthdayOneBefore.setLogin("testlogin");
		userCorrectBirthdayOneBefore.setName("testname");
		userCorrectBirthdayOneBefore.setEmail("test4545@mail.com");

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			User result = userService.create(userCorrectBirthdayOneBefore);
			assertEquals(LocalDate.now().minusDays(1),result.getBirthday());
		});

		//когда день рождения в текущем дне
		//когда день рождения на один день раньше будущего
		User userCorrectBirthdayNow = new User();
		userCorrectBirthdayNow.setBirthday(LocalDate.now());
		userCorrectBirthdayNow.setLogin("testlogin");
		userCorrectBirthdayNow.setName("testname");
		userCorrectBirthdayNow.setEmail("test454@mail.com");

		//проверяем, что ошибка не была поймана
		assertDoesNotThrow(() -> {
			User result = userService.create(userCorrectBirthdayNow);
			assertEquals(LocalDate.now(),result.getBirthday());
		});

		//когда на один день позде будующего
		User userCorrectBirthdayOneAfter = new User();
		userCorrectBirthdayOneAfter.setBirthday(LocalDate.now().plusDays(1));
		userCorrectBirthdayOneAfter.setLogin("testlogin");
		userCorrectBirthdayOneAfter.setName("testname");
		userCorrectBirthdayOneAfter.setEmail("test55456@mail.com");

		//проверяем, что ошибка была поймана
		assertThrows(ValidationException.class,() -> userService.create(userCorrectBirthdayOneAfter));
	}
}