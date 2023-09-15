# Задание №5. Key-Value Store

В этом задании вам предстоит реализовать собственное
персистентное [key-value хранилище](https://en.wikipedia.org/wiki/Key%E2%80%93value_database). Про
такие системы можно
интуитивно думать как про `Map`, которая сохраняет информацию на диск. `key-value`
хранилища используются повсеместно – от системы индексов в вашей любимой IDE до хранения фотографий
в некоторых
социальных сетях.

Наша система будет состоять из следующих частей:

1. `KeyValueStore` - класс для взаимодействия с базой данных, управляет `IndexManager`
   и `ValueStoreManager`.
2. `IndexManager` - сущность, которая занимается чтением/записью **ключей** на диск и поддержанием
   связи `key-value`.
3. `ValueStoreManager` - сущность, которая занимается чтением/записью **значений** на диск.

На систему накладываются следующие ограничения:

1. Значения хранятся в файлах ограниченного размера (максимальный размер файла передается
   в `KeyValueStoreFactory#create`). Если при записи значения закончился текущий файл, необходимо
   создать новый и
   продолжить записывать в него. Указатели на эти блоки значений описываются с помощью
   дескрипторов `FileBlockLocation`.

   Важные детали:
    * Одно значение может быть разбито на несколько кусков, которые хранятся в разных файлах.

      Например, это нужно, когда размер значения больше, чем максимальный размер одного файла. В
      этом случае его
      приходится разбивать на куски.
    * В одном файле может храниться сразу несколько значений или их кусков.
2. `IndexManager` инициализирует и поддерживает **индексный файл**, который хранит
   связи `key -> List<FileBlockLocation>`.
3. Для упрощения задачи в базе не будет честной физической операции удаления. Удаление будет только
   логическим - для
   этого достаточно удалить соответствующую связь в `IndexManager` и пометить занятые значением
   блоки как свободные. Список свободных блоков проще всего поддерживать внутри `ValueStoreManager`
   с помощью дополнительного индексного файла.
4. В дальнейшем, для экономии памяти, значения должны сначала записываться в свободные блоки. Новые
   файлы должны
   создаваться только в случае, когда свободных блоков не осталось.

## Советы и ограничения по реализации

* Не забывайте, что файлы на диске могут быть сколь угодно большими - избегайте считывать их в
  память целиком.
* Скорее всего, для работы с файлами вам понадобится `RandomAccessFile`. Посмотрите, какие методы у
  него есть и как с ним работать.
    * Особо обратите внимание на семейство методов `write*`.
* Также можете посмотреть на класс `DataInput/OutputStream`.
* **ВАЖНО**: В данном задании **запрещается** использовать классы `ObjectInputStream` и
  `ObjectOutputStream` для сохранения информации на диск. Такое требование накладывается для того,
  чтобы ваша реализация не была привязана к сериализации Java-объектов.

## Задачи

### 1. Реализуйте `IndexManager` [4 балла]

При инициализации пустой базы `IndexManager` должен создавать индексный файл в рабочей директории
(она передается при инициализации системы через `KeyValueStoreFactory#create`). Индексный файл 
должен обновляться свежей информацией как минимум при вызове `#close`.

### 2. Реализуйте `ValueStoreManager` [6 баллов]

Главная задача `ValueStoreManager` - писать и читать значения из файлов. Для этого ему необходимо
поддерживать
информацию о свободных блоках в файлах. При удалении по ключу занятые блоки должны перемещаться в
пул свободных блоков.
При записи нужно брать свободные блоки и писать сначала в них, а потом дописывать в новые файлы.
После того как
очередное значение было записано, нужно добавить соответствующую запись в индекс. Для
этого `ValueStoreManager#add`
возвращает набор записанных блоков, которые нужно передать в `IndexManager`.

### 2.1. Реализуйте `ValueStoreManager#openBlockStream` [2 балла]

Для удобства чтения из блока нужно написать собственную имплементацию `InputStream`, которая будет
уметь читать *один*
блок. Значение может быть разбито на несколько блоков - в этом случае можно открыть по потоку для
каждого из них, а
затем объединить их в один поток, используя класс `java.io.SequenceInputStream`.

### 3. Реализуйте `KeyValueStore` и `KeyValueStoreFactory` [2 балла]

Все запчасти готовы, осталось собрать:)

P.S. Стоит внимательно подумать про работу с исключениями.

## Как сдавать

При создании вашего репозитория будет автоматически создан [Pull Request для проверки](../../pull/1).

Вы должны закоммитить своё решение в ветку `main` (это ветка вашего репозитория по-умолчанию), и эти
коммиты
автоматически добавятся в Pull Request.

**Этот пулл-реквест не нужно мерджить и не нужно закрывать!!!**

Когда вы будете готовы сдать решение на проверку, добавьте к вашему пулл-реквесту
лейбл `ready-for-review`. Это можно сделать в правой части страницы с пулл-реквестом.

В дальнейшем, если вы внесли исправления и хотите запросить очередную проверку, просто повторно
запросите ревью от
преподавателя, который вас уже проверял:

![Анимация того, как запросить ревью](https://i.stack.imgur.com/H2XaO.gif)

Если у вас возникают проблемы на каком-то из этих шагов, пожалуйста, сообщите об этом в Slack. Чем
быстрее вы это
сделаете, тем быстрее мы поможем вам.