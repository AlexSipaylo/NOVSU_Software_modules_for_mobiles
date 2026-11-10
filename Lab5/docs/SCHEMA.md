# Упрощённая схема БД FinTrack

```text
accounts (1) ─────── (N) transactions (N) ─────── (1) categories
                                                │
                                                │ (1)
                                                │
                                                (N) category_limits

budget_rules       — справочник правил контроля лимита
recurring_payments — список регулярных платежей для напоминаний
```

| Таблица | Ключевые поля | Назначение |
|---|---|---|
| accounts | id, name | Счёт; при первом запуске создаётся «Основной счёт» |
| categories | id, name, type | Справочник категорий доходов и расходов |
| transactions | id, accountId, categoryId, type, amountKopecks, date | Доходы и расходы |
| category_limits | id, categoryId, month, limitKopecks | Месячный лимит категории |
| budget_rules | id, minPercent, severity, title, color, icon | Порог, уровень, цвет и значок предупреждения |
| recurring_payments | id, title, amountKopecks, dueDate, active | Платёж и дата следующего напоминания |

## Внешние ключи

- `transactions.accountId → accounts.id`, `ON DELETE CASCADE`: при удалении счёта удаляются его операции.
- `transactions.categoryId → categories.id`, `ON DELETE RESTRICT`: нельзя удалить категорию, которая уже используется в истории.
- `category_limits.categoryId → categories.id`, `ON DELETE CASCADE`: при удалении категории удаляется её лимит.

Индексы созданы на внешних ключах. Уникальный индекс `(categoryId, month)` не позволяет назначить две суммы лимита одной категории в одном месяце.

## Правила бюджета

| minPercent | severity | Значение |
|---:|---:|---|
| 0 | 0 | В пределах нормы |
| 80 | 1 | Приближение к лимиту |
| 100 | 2 | Лимит превышен |
| 120 | 3 | Существенное превышение |
