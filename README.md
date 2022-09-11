
# EasyFeedbackBackend

#### 1. Mobile App => Backend App
                    
```seq
Flutter mobile app->BACKEND APP\n(SPRING): feedback data 
Note right of BACKEND APP\n(SPRING): process \nfeedback data 
Note right of BACKEND APP\n(SPRING): sentiment analysis \non data\nusing  Google apis 
BACKEND APP\n(SPRING)->Database MySQL: persist processed\nfeedback data 
BACKEND APP\n(SPRING)-->SSE: processed data 
```

#### 2. Web App => Backend App
                    
```seq
Flutter Web App->BACKEND APP\n(SPRING): login 
BACKEND APP\n(SPRING)->Flutter Web App: login success\nif correct credentials 
Flutter Web App->BACKEND APP\n(SPRING): fetch last\nN feedbacks 
Database MySQL->BACKEND APP\n(SPRING): pull N\nfeedback data 
BACKEND APP\n(SPRING)->Flutter Web App: N feedback data 
Flutter Web App->BACKEND APP\n(SPRING): live feedback data 
SSE-->Flutter Web App:live data 
```

##### SSE : Server Sent Events for real time updates.
