# Задание №2 (Вариант 15)

Разработать инструмент командной строки для визуализации графа
зависимостей, включая транзитивные зависимости. Сторонние средства для
получения зависимостей использовать нельзя.
Зависимости определяются для git-репозитория. Для описания графа
зависимостей используется представление Graphviz. Визуализатор должен
выводить результат на экран в виде графического изображения графа.
75
Построить граф зависимостей для коммитов, в узлах которого находятся
связи с файлами и папками, представленными уникальными узлами.
Конфигурационный файл имеет формат xml и содержит:
• Путь к программе для визуализации графов.
• Путь к анализируемому репозиторию.

<img src="Image/graph.graphviz.png">
