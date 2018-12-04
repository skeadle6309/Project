#   CEG 4110 Fall 2018 Project
##   Seth Keadle Alex Voultos Daniel Howard
![alt text](https://github.com/skeadle6309/Project/blob/master/Screenshot_1543943534.png)

This project is developed for the android os and has a minSDK of 16 and a target sdk of 26. This allows users from Kit-Kat and newer to run and compile this application. The application was developed in the Android Studio SDK.

##   Overview
This application allows a user to select a photo from the gallery or capture a photo form the phone's camera and submit it to a server. The server is running  apython scrupt that evaluates the photo via tensorflow machine learning for the prescence of food or no food. The results from the python script are then returned to the user in the form of a percentage scale above the photo. The application also submits each photo and the tensorflow results to the server's database.

##   Application
The user is first presented with three buttons "seefood", "View the Database", and "GetPhoto". The getfood button brings up the image picker and allows the user to select a photo from the gallery or capture a photo from the camera. After a photo is selected the current photo is displayed to the user on the menu screen. Then the user can press seefood to view the tensorflow results of that specific image. The third button is the "View the Database" button where the user can scroll through all images ever evaluated via "seefood" by pressing the next or previous image btn. 

### Libraries and instructions
####    esafirm/android-image-picker
A simple gradle dependency add
#### pkleczko/CustomGauge
add the gradle dependency along with adding the custom gauge xml layout to the activities.xml layout.
![alt text]https://github.com/skeadle6309/Project/blob/master/Screenshot_1543943558.png
![alt text]https://github.com/skeadle6309/Project/blob/master/Screenshot_1543943568.png
![alt text]https://github.com/skeadle6309/Project/blob/master/Screenshot_1543943588.png
![alt text]https://github.com/skeadle6309/Project/blob/master/Screenshot_1543943664.png
