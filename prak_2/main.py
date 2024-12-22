import os
import json
import zlib
from graphviz import Digraph

def parse_object(object_hash, description=None):
    # Полный путь к объекту по его хэшу
    object_path = os.path.join(config['repo_path'], '.git', 'objects', object_hash[:2], object_hash[2:])

    # Открываем git-объект
    with open(object_path, 'rb') as file:
        # Разжали объект, получили его сырое содержимое
        raw_object_content = zlib.decompress(file.read())
        # Разделили содержимое объекта на заголовок и основную часть
        header, raw_object_body = raw_object_content.split(b'\x00', maxsplit=1)
        # Извлекли из заголовка информацию о типе объекта и его размере
        object_type, content_size = header.decode().split(' ')

        # Словарь с данными git-объекта:
        # {
        #   'label': текстовая метка, которая будет отображаться на графе
        #   'children': список из детей этого узла (зависимых объектов)
        # }
        object_dict = {}

        # В зависимости от типа объекта используем разные функции для его разбора
        if object_type == 'commit':
            object_dict['label'] = r'[commit]\n' + object_hash[:6]
            object_dict['children'] = parse_commit(raw_object_body)

        elif object_type == 'tree':
            object_dict['label'] = r'[tree]\n' + object_hash[:6]
            object_dict['children'] = parse_tree(raw_object_body)

        elif object_type == 'blob':
            object_dict['label'] = r'[blob]\n' + object_hash[:6]
            object_dict['children'] = []

        # Добавляем дополнительную информацию, если она была
        if description is not None:
            object_dict['label'] += r'\n' + description

        return object_dict


def parse_tree(raw_content):
    # Дети дерева (соответствующие строкам объекта)
    children = []

    # Парсим данные, последовательно извлекая информацию из каждой строки
    rest = raw_content
    while rest:
        # Извлечение режима
        mode, rest = rest.split(b' ', maxsplit=1)
        # Извлечение имени объекта
        name, rest = rest.split(b'\x00', maxsplit=1)
        # Извлечение хэша объекта и его преобразование в 16ричный формат
        sha1, rest = rest[:20].hex(), rest[20:]
        # Добавляем потомка к списку детей
        children.append(parse_object(sha1, description=name.decode()))

    return children


def parse_commit(raw_content):
    # Переводим raw_content в кодировку UTF-8 (до этого он был последовательностью байтов)
    content = raw_content.decode()
    # Делим контент на строки
    content_lines = content.split('\n')

    # Словарь с содержимым коммита
    commit_data = {}

    # Извлекаем хэш объекта дерева, привязанного к коммиту
    commit_data['tree'] = content_lines[0].split()[1]
    content_lines = content_lines[1:]

    # Список родительских коммитов
    commit_data['parents'] = []
    # Парсим всех родителей, сколько бы их ни было
    while content_lines[0].startswith('parent'):
        commit_data['parents'].append(content_lines[0].split()[1])
        content_lines = content_lines[1:]

    # Извлекаем информацию об авторе и коммитере
    while content_lines[0].strip():
        key, *values = content_lines[0].split()
        commit_data[key] = ' '.join(values)
        content_lines = content_lines[1:]

    # Извлекаем сообщение к комиту
    commit_data['message'] = '\n'.join(content_lines[1:]).strip()
    
    # Возвращаем все зависимости объекта коммита (то есть его дерево и всех родителей)
    return [parse_object(commit_data['tree'])] + \
           [parse_object(parent) for parent in commit_data['parents']]


def get_last_commit():
    """Получить хэш для последнего коммита в ветке"""
    head_path = os.path.join(config['repo_path'], '.git', 'refs', 'heads', config['branch'])
    with open(head_path, 'r') as file:
        return file.read().strip()

def recursive_add_edges(a, tree):
    """Рекурсивно перебрать все узлы дерева для построения графа"""
    label = tree['label']
    for child in tree['children']:
        a.edge(label, child['label'])
        recursive_add_edges(a, child)

def generate_graphviz(filename):
    """Создать граф зависимостей с помощью библиотеки Graphviz"""

    # Стартовая точка репозитория - последний коммит главной ветки
    last_commit = get_last_commit()
    print(last_commit)
    # Строим дерево
    tree = parse_object(last_commit)
    # Создаем граф
    a = Digraph()
    recursive_add_edges(a, tree)
    # Сохраняем граф
    a.render(filename, format='png')


# Достаем информацию из конфигурационного файла
with open('config.json', 'r') as f:
    config = json.load(f)

# Генерируем файл с DOT-нотацией графа зависимостей
generate_graphviz('graph.graphviz')
