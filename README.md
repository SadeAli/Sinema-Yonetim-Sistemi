# Bilet Satış Sistemi
Bir sinema salonunun koltuk satışlarının kolay bir şekilde düzenlenebilmesi
ve yönetilebilmesi için hazırlanmış bir **İşlemsel Bilgi Sistemi**

## Kurulum
### Derlemek için
```
javac -cp "dependencies/*" -d bin src/cinema/*.java src/database/*.java src/gui/*.java src/gui/mainPanels/*.java src/gui/mainPanels/adminPanels/*.java src/gui/guiUtils/*.java
```
### Çalıştırmak için
```
java -cp "bin;dependencies/*" gui.CinemaGUI
```

## İçindekiler
[Sistemin Amacı](#sistemin-amac%C4%B1)  
[Sistemin Bileşenleri](#sistemin-bile%C5%9Fenleri)  
[Modül Alt Görevleri](#mod%C3%BCl-alt-g%C3%B6revleri)

### Sistemin Amacı  
bilet satışlarının düzenlenmesi ve bilet satış istatistiklerinin tutulması

### Sistemin Bileşenleri  
1 - film tarih ve takvim modülü  
2 - fiyatlandırma ve kampanya modülü  
3 - koltuk takip modülü  
4 - ödeme modülü  
5 - istatistik modülü  

### Modül Alt Görevleri  

#### 1 - film tarih ve takvim modülü  
filmlerin listelenmesi  
tarih seçimi  
film seçimi  
seans seçimi  

#### 2 - fiyatlandırma ve kampanya modülü  
özel günlerin belirlenmesi  
fiyatlandırma  
kampanya seçimi  
kampanya bilgileri  

#### 3 - koltuk takip modülü  
boş/dolu koltukların görüntülenmesi  
koltuk seçimi  
seçilen koltukların işaretlenmesi  

#### 4 - ödeme modülü  
ödeme seçeneklerinin belirlenmesi  
ödeme bilgilerinin girilmesi  
ödeme işlemi  

#### 5 - istatistik modülü  
belirli bir tarih aralığındaki bilet satışlarının listelenmesi  
en az ve en çok satılan film ve seansların listelenmesi  
toplam gelirin hesaplanması  
