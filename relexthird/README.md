В входном файле input.txt дополнительно задается массивом время полива и время перемещения до клумбы. Соответственно вносятся изменения в класс InputInfo для работы с json.
Также изменяются инициализация поливочной машины при смене клумбы.
Код:
machine.init(flowerbeds[currentFlowerbed].getTimeToMove(), flowerbeds[currentFlowerbed].getTimeToShower()); 
