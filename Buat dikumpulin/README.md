# CACING IRON MEN
## Pemanfaatan Algoritma Greedy dalam Aplikasi Permainan “Worms”

### *Tugas Besar I IF2211 Strategi Algoritma*

*Program Studi Teknik Informatika* <br />
*Sekolah Teknik Elektro dan Informatika* <br />
*Institut Teknologi Bandung* <br />

*Semester II Tahun 2020/2021*

## Algoritma Greedy
Pada bot ini, kelompok kami memilih untuk menggunakan algoritma **Greedy by Ganking *Commander* and Regroup**. <br />
Dalam algoritma ini, worm *agent* dan *technologist* akan menyerang *commander* lawan, sedangkan worm *commander* akan berusaha untuk berkumpul dengan worm *agent* dan *technologist*. 
Jika worm *commander* lawan sudah mati, maka semua worm yang tersisa akan memburu worm lawan yang paling dekat.

## Requirement
- [Java Development Kit](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- [Java Virtual Enviroment](https://www.java.com/en/download/)
- [IntelliJ IDEA](https://www.jetbrains.com/idea/download/)

## Executing
- Buka file `game-runner-config.json` pada folder pemainan Worm
- Ubah nilai parameter `"player.a"` menjadi alamat folder java di dalam folder src, contoh :`"player-a": "./Tubes1_13519008/src/java" `
- Alternatif, ubah nilai parameter `"player.a"` menjadi alamat folder src di dalam bot ini, contoh :`"player-a": "./Tubes1_13519008/bin"
- Jalankan permainan worm

## Author
- Ronggur Mahendra Widya Putra (13519008)
- Muhammad Azhar Faturahman (13519020)
- Syihabuddin Yahya Muhammad (13519149)
