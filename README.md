# Task 3: Hazelcast logging
executed by: Vladislav Razoronov(https://github.com/VladislavRazoronov)

У разі вимкнення одого або кількох loggingService, спроба GET
запиту до facadeService починає повертати Error через відсутність
активного сервісу, що записаний локально в списку, але якщо випадково
обирається працюючий сервіс, то всі повідомлення є видими з нього
