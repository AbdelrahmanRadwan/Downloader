# Questions

## How did you approach solving the problem?
- Instead of downloading the whole file at once, I used content range download. 

- To stop the download, I kept a flag that indicates if the download is active or not.

- To test the features, I used multiple threads, and a thread with interruption.

- **References I used:**
  - https://www.rfc-editor.org/rfc/rfc9110.html#section-14.4
  - https://kenstechtips.com/index.php/download-speeds-2g-3g-and-4g-actual-meaning

## How did you verify your solution works correctly?
- I used a readable sample of downloadable data from here: https://norvig.com/big.txt to verify that the content is being written correctly and completely to the file. In addition to this, I used the file progress metrics to measure the percentage of the downloaded content in comparison to the total size of the file that I knew from the headers. 


## How long did you spend on the exercise?
- One hour, and thirty-three minutes. (In addition to a few minutes to answer these questions and submit the results).

## What would you add if you had more time and how?
- Instead of using hardcoded values for the chunk that is safe to assume that it will be downloaded per second. I could have checked the current internet speed of the client and adjusted the offset size based on this. However, I don't know exactly how to do this, I would have needed to search online to find.
- Use multithreading with buckets to download the content instead of one single thread.
- A UI so I can test the interrupt feature. 
- The skeleton code is using old style Java. I could update this to use Java 17 and Lombok. However, I was worried that you wouldn't have these downloaded.
- More Unite Tests.
- Graphical way of representing the file progress metrics.
