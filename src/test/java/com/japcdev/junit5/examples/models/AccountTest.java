package com.japcdev.junit5.examples.models;

import com.japcdev.junit5.examples.exceptions.InsufficientBalanceException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

// Para indicar el ciclo de vida de la clase, en estes caso es se indica por clase,
// por lo que solo habrá una sola instancia de la clase test para todos los métodos.
// No es muy recomendable.
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountTest {
    private Account account;

    @BeforeAll
    static void beforeAll() {
        System.out.println("Test init - class");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Test final - class");
    }

    @BeforeEach
    void initTest() {
        this.account = new Account("Juan Antonio", new BigDecimal("1000.12345"));
    }

    @Nested
    @DisplayName("Testing account attributes")
    class AccountTestNameAndBank {
        // BeforeEach y AfterEach se puede poner para cada una de las clases anidadas.
        @BeforeEach
        void initTest() {
            System.out.println("Init test method");
        }

        @AfterEach
        void tearDown() {
            System.out.println("Finally test method");
        }

        @Test
        @DisplayName("Name")
        void testNameAccount() {
//        account.setName("Juan Antonio");

            String expectedResult = "Juan Antonio";
            String actualResult = account.getName();

            assertNotNull(actualResult, () -> "The account can not be null");

            assertEquals(expectedResult, actualResult, () -> "Account name is not as expected. Expected name: "
                    + expectedResult + ", result: " + actualResult);

            assertTrue(actualResult.equals("Juan Antonio"), () -> "Expected result must be the same as actual result");
        }

        @Test
        @DisplayName("Balance")
        void testAccountBalance() {
            assertEquals(1000.12345, account.getBalance().doubleValue());

            // Dos formas para comprobar que el saldo es mayor que cero.
            assertFalse(account.getBalance().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("References to be equal with the equals method")
        void testAccountRef() {
            Account account1 = new Account("Test1", new BigDecimal("9000.00007"));
            Account account2 = new Account("Test1", new BigDecimal("9000.00007"));

//        assertNotEquals(account2, account1);
            assertEquals(account2, account1);
        }

        @Test
        @DisplayName("Account and bank relationships with assertAll")
        void testRelationBankAccount() {
            Account account1 = new Account("User1", new BigDecimal("2500"));
            Account account2 = new Account("User2", new BigDecimal("1500.12345"));

            Bank bank = new Bank();

            bank.addAccount(account1);
            bank.addAccount(account2);
            bank.setName("Imagic Bank");
            bank.transfer(account2, account1, new BigDecimal(500));

            assertAll(
                    () -> assertEquals("1000.12345", account2.getBalance().toPlainString()),
                    () -> assertEquals("3000", account1.getBalance().toPlainString()),
                    () -> assertEquals(2, bank.getAccounts().size()),
                    () -> assertEquals("Imagic Bank", account1.getBank().getName()),
                    () -> assertEquals("User1", bank.getAccounts().stream()
                            .filter(c -> c.getName().equals("User1"))
                            .findFirst()
                            .get().getName()
                    ),
                    // comprobar que el user1 existe en el banco
                    // Dos formas de hacer la misma comprobacion
                    () -> assertTrue(bank.getAccounts().stream()
                            .filter(c -> c.getName().equals("User1"))
                            .findFirst().isPresent()),
                    () -> assertTrue(bank.getAccounts().stream()
                            .anyMatch(c -> c.getName().equals("User2")))
            );
        }
    }

    @Nested
    class AccountOpertations {
        @Test
        void testDebitAccount() {
            account.debit(new BigDecimal("100"));

            assertNotNull(account.getBalance());
            assertEquals(900, account.getBalance().intValue());

            assertEquals("900.12345", account.getBalance().toPlainString());
        }

        @Test
        void testCreditAccount() {

            account.credit(new BigDecimal("100"));

            assertNotNull(account.getBalance());
            assertEquals(1100, account.getBalance().intValue());

            assertEquals("1100.12345", account.getBalance().toPlainString());
        }

        @Test
        void testTransferMoney() {
            Account account1 = new Account("User1", new BigDecimal("2500"));
            Account account2 = new Account("User2", new BigDecimal("1500.12345"));
            Bank bank = new Bank();
            bank.transfer(account2, account1, new BigDecimal("500"));

            assertEquals("1000.12345", account2.getBalance().toPlainString());
            assertEquals("3000", account1.getBalance().toPlainString());
        }
    }

    @Nested
    class AccountExceptions {
        @Test
        void insufficientBalanceExceptionAccount() {
            Account account = new Account("test", new BigDecimal("500.00"));

            // Se comprueba que salta la excepcion cuando no hay suficiente saldo
            Exception exception = assertThrows(InsufficientBalanceException.class, () -> {
                account.debit(new BigDecimal("600"));
            });

            // Se comprueba que el mensaje de salida es igual al esperado.
            String actualMessage = exception.getMessage();
            String expectedMessege = "Insufficient balance";

            assertEquals(expectedMessege, actualMessage);
        }
    }

    @Nested
    class OSTest {
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testOnlyWindows() {

        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testOnlyLinuxMac() {

        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void noWindows() {

        }
    }

    @Nested
    class JavaVersionTest {
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void onlyJDK8() {

        }

        @Test
        @EnabledOnJre(JRE.JAVA_15)
        void onlyJDK15() {

        }

        @Test
        @DisabledOnJre(JRE.JAVA_15)
        void noJDK15() {

        }
    }

    @Nested
    class SystemProperties {
        @Test
        void printSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((key, value) -> System.out.println(key + ": " + value));
        }

        @Test
        @EnabledIfSystemProperty(named = "os.name", matches = "Windows 10")
        void onlyWindows10() {

        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = "15.0.2")
        void onlyJavaVersion() {

        }

        @Test
        @DisabledIfSystemProperty(named = "sun.arch.data.model", matches = "64")
        void onlyOS32bit() {

        }

        @Test
        @EnabledIfSystemProperty(named = "ENV", matches = "dev")
        void onlyDev() {
        }
    }

    @Nested
    class EnvironmentVariablesTest {
        @Test
        void printEnvironmentVariables() {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((key, value) -> System.out.println(key + ": " + value));

        }

        @Test
        @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = ".*jdk-8.0.282.8-hotspot.*")
        void javaHome() {

        }

        @Test
        @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS:", matches = "4")
        void numberOfProcessors() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
        void onlyDevEnvironment() {
        }
    }

    @Nested
    class AssumptionsTest {
        @Test
        void testAccountBalanceWithAssume() {
            boolean isDevEnvironment = "dev".equals(System.getProperty("ENV"));

            // Deshabilita el test si no se cumple
            assumeTrue(isDevEnvironment);

            assertEquals(1000.12345, account.getBalance().doubleValue());
            assertFalse(account.getBalance().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        void testAccountBalanceWithAssumingThat() {
            boolean isDevEnvironment = "pro".equals(System.getProperty("ENV"));

            // Solo ejecuta estos metodos si se cumple
            assumingThat(isDevEnvironment, () -> {
                assertEquals(1000.12345, account.getBalance().doubleValue());
                assertFalse(account.getBalance().compareTo(BigDecimal.ZERO) < 0);
            });

            assertTrue(account.getBalance().compareTo(BigDecimal.ZERO) > 0);

        }
    }
}