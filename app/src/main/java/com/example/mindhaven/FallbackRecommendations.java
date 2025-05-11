package com.example.mindhaven;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides fallback recommendations when API calls fail
 */
public class FallbackRecommendations {

    // Movies by mood
    private static final Map<String, List<Recommendation>> MOVIES_BY_MOOD = new HashMap<>();
    
    // Music by mood
    private static final Map<String, List<Recommendation>> MUSIC_BY_MOOD = new HashMap<>();
    
    // Books by mood
    private static final Map<String, List<Recommendation>> BOOKS_BY_MOOD = new HashMap<>();
    
    static {
        initializeMovies();
        initializeMusic();
        initializeBooks();
    }
    
    /**
     * Initialize fallback movie recommendations
     */
    private static void initializeMovies() {
        // Happy movies
        List<Recommendation> happyMovies = new ArrayList<>();
        happyMovies.add(new Recommendation("The Intouchables", "A comedic tale of friendship between a wealthy quadriplegic and his caregiver", "happy", "movie"));
        happyMovies.add(new Recommendation("Forrest Gump", "The presidencies of Kennedy and Johnson, the Vietnam War, and other historical events unfold through the perspective of an Alabama man with an IQ of 75", "happy", "movie"));
        happyMovies.add(new Recommendation("La La Land", "While navigating their careers in Los Angeles, a pianist and an actress fall in love while attempting to reconcile their aspirations for the future", "happy", "movie"));
        happyMovies.add(new Recommendation("The Grand Budapest Hotel", "A concierge at a famous European hotel between the wars and his trusted lobby boy become wrapped up in a caper", "happy", "movie"));
        happyMovies.add(new Recommendation("Toy Story", "A cowboy doll is profoundly threatened when a new spaceman action figure supplants him as top toy in a boy's bedroom", "happy", "movie"));
        // Adding more happy movies
        happyMovies.add(new Recommendation("Mamma Mia!", "The story of a bride-to-be trying to find her real father told using hit songs by the popular 1970s group ABBA", "happy", "movie"));
        happyMovies.add(new Recommendation("Paddington", "A young Peruvian bear travels to London in search of a home", "happy", "movie"));
        happyMovies.add(new Recommendation("Sing Street", "A boy growing up in Dublin during the 1980s escapes his strained family life by starting a band", "happy", "movie"));
        happyMovies.add(new Recommendation("School of Rock", "After being kicked out of his rock band, Dewey Finn becomes a substitute teacher of an uptight elementary private school", "happy", "movie"));
        happyMovies.add(new Recommendation("Little Miss Sunshine", "A family determined to get their young daughter into the finals of a beauty pageant take a cross-country trip in their VW bus", "happy", "movie"));
        MOVIES_BY_MOOD.put("happy", happyMovies);
        
        // Sad movies
        List<Recommendation> sadMovies = new ArrayList<>();
        sadMovies.add(new Recommendation("The Shawshank Redemption", "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency", "sad", "movie"));
        sadMovies.add(new Recommendation("The Pursuit of Happyness", "A struggling salesman takes custody of his son as he's poised to begin a life-changing professional career", "sad", "movie"));
        sadMovies.add(new Recommendation("Life Is Beautiful", "When an open-minded Jewish waiter and his son become victims of the Holocaust, he uses a perfect mixture of will, humor, and imagination to protect his son", "sad", "movie"));
        sadMovies.add(new Recommendation("The Green Mile", "The lives of guards on Death Row are affected by one of their charges: a black man accused of child murder and rape, yet who has a mysterious gift", "sad", "movie"));
        sadMovies.add(new Recommendation("Pay It Forward", "A young boy attempts to make the world a better place after his teacher gives him that chance", "sad", "movie"));
        // Adding more sad movies
        sadMovies.add(new Recommendation("A Star is Born", "A musician helps a young singer find fame as age and alcoholism send his own career into a downward spiral", "sad", "movie"));
        sadMovies.add(new Recommendation("Room", "A woman who was kidnapped as a teenager and held captive for years in a tiny shed with her son finally escapes, allowing the boy to experience the outside world for the first time", "sad", "movie"));
        sadMovies.add(new Recommendation("The Fault in Our Stars", "Two teenagers with cancer meet at a support group and fall in love", "sad", "movie"));
        sadMovies.add(new Recommendation("Schindler's List", "In German-occupied Poland during World War II, Oskar Schindler gradually becomes concerned for his Jewish workforce after witnessing their persecution by the Nazi Germans", "sad", "movie"));
        sadMovies.add(new Recommendation("Manchester by the Sea", "A depressed uncle is asked to take care of his teenage nephew after the boy's father dies", "sad", "movie"));
        MOVIES_BY_MOOD.put("sad", sadMovies);
        
        // Excited movies
        List<Recommendation> excitedMovies = new ArrayList<>();
        excitedMovies.add(new Recommendation("Avengers: Endgame", "After the devastating events of Infinity War, the universe is in ruins. The Avengers assemble once more to undo Thanos' actions", "excited", "movie"));
        excitedMovies.add(new Recommendation("Mission: Impossible - Fallout", "Ethan Hunt and his IMF team race against time after a mission gone wrong", "excited", "movie"));
        excitedMovies.add(new Recommendation("Top Gun: Maverick", "After more than thirty years of service as a top naval aviator, Pete Mitchell is where he belongs, pushing the envelope as a courageous test pilot", "excited", "movie"));
        excitedMovies.add(new Recommendation("Mad Max: Fury Road", "In a post-apocalyptic wasteland, a woman rebels against a tyrannical ruler in search for her homeland with the aid of a group of female prisoners", "excited", "movie"));
        excitedMovies.add(new Recommendation("The Dark Knight", "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice", "excited", "movie"));
        // Adding more excited movies
        excitedMovies.add(new Recommendation("John Wick", "An ex-hit-man comes out of retirement to track down the gangsters that killed his dog and took everything from him", "excited", "movie"));
        excitedMovies.add(new Recommendation("Fast & Furious 7", "Dominic Toretto and his crew are targeted by the vengeful brother of Owen Shaw", "excited", "movie"));
        excitedMovies.add(new Recommendation("Gladiator", "A former Roman General sets out to exact vengeance against the corrupt emperor who murdered his family and sent him into slavery", "excited", "movie"));
        excitedMovies.add(new Recommendation("Edge of Tomorrow", "A soldier fighting aliens gets to relive the same day over and over again, the day restarting every time he dies", "excited", "movie"));
        excitedMovies.add(new Recommendation("Spider-Man: Into the Spider-Verse", "Teen Miles Morales becomes the Spider-Man of his universe, and must join with five spider-powered individuals from other dimensions to stop a threat for all realities", "excited", "movie"));
        MOVIES_BY_MOOD.put("excited", excitedMovies);
        
        // Scared movies
        List<Recommendation> scaredMovies = new ArrayList<>();
        scaredMovies.add(new Recommendation("The Princess Bride", "A fairy tale adventure about a beautiful young woman and her one true love", "scared", "movie"));
        scaredMovies.add(new Recommendation("Spirited Away", "During her family's move to the suburbs, a sullen 10-year-old girl wanders into a world ruled by gods, witches, and spirits", "scared", "movie"));
        scaredMovies.add(new Recommendation("Harry Potter and the Sorcerer's Stone", "An orphaned boy enrolls in a school of wizardry, where he learns the truth about himself, his family and the terrible evil that haunts the magical world", "scared", "movie"));
        scaredMovies.add(new Recommendation("The Wizard of Oz", "Young Dorothy Gale is swept away by a tornado from her Kansas farm to the magical Land of Oz", "scared", "movie"));
        scaredMovies.add(new Recommendation("Inside Out", "After young Riley is uprooted from her Midwest life and moved to San Francisco, her emotions conflict on how best to navigate a new city, house, and school", "scared", "movie"));
        // Adding more comforting movies for scared
        scaredMovies.add(new Recommendation("Ratatouille", "A rat who can cook makes an unusual alliance with a young kitchen worker at a famous Paris restaurant", "scared", "movie"));
        scaredMovies.add(new Recommendation("The Secret Garden", "A young, recently orphaned girl discovers a magical garden hidden at her strict uncle's estate", "scared", "movie"));
        scaredMovies.add(new Recommendation("The Lego Movie", "An ordinary LEGO construction worker is recruited to join a quest to stop an evil tyrant from gluing the LEGO universe into eternal stasis", "scared", "movie"));
        scaredMovies.add(new Recommendation("My Neighbor Totoro", "When two girls move to the country to be near their ailing mother, they have adventures with the wondrous forest spirits who live nearby", "scared", "movie"));
        scaredMovies.add(new Recommendation("Up", "78-year-old Carl Fredricksen travels to Paradise Falls in his house equipped with balloons, inadvertently taking a young stowaway", "scared", "movie"));
        MOVIES_BY_MOOD.put("scared", scaredMovies);
        
        // Disappointed movies
        List<Recommendation> disappointedMovies = new ArrayList<>();
        disappointedMovies.add(new Recommendation("The King's Speech", "The story of King George VI, who unexpectedly became king and his struggle to overcome his stammering", "disappointed", "movie"));
        disappointedMovies.add(new Recommendation("Hacksaw Ridge", "The true story of Desmond T. Doss, who, in Okinawa during WWII, won the Medal of Honor for his incredible bravery and devotion to his fellow soldiers", "disappointed", "movie"));
        disappointedMovies.add(new Recommendation("Schindler's List", "In German-occupied Poland during World War II, Oskar Schindler gradually becomes concerned for his Jewish workforce after witnessing their persecution by the Nazi Germans", "disappointed", "movie"));
        disappointedMovies.add(new Recommendation("Hidden Figures", "The story of a team of female African-American mathematicians who served a vital role in NASA during the early years of the U.S. space program", "disappointed", "movie"));
        disappointedMovies.add(new Recommendation("12 Years a Slave", "In the antebellum United States, Solomon Northup, a free black man, is abducted and sold into slavery", "disappointed", "movie"));
        // Adding more inspiring movies for disappointed
        disappointedMovies.add(new Recommendation("The Blind Side", "The story of Michael Oher, a homeless and traumatized boy who became an All-American football player with the help of a caring woman and her family", "disappointed", "movie"));
        disappointedMovies.add(new Recommendation("Invictus", "Nelson Mandela, in his first term as President of South Africa, initiates a unique venture to unite the Apartheid-torn land: enlist the national rugby team to win the 1995 Rugby World Cup", "disappointed", "movie"));
        disappointedMovies.add(new Recommendation("The Imitation Game", "The story of Alan Turing, who created a machine that helped decipher the Enigma code during WWII", "disappointed", "movie"));
        disappointedMovies.add(new Recommendation("127 Hours", "A mountain climber becomes trapped under a boulder while canyoneering alone near Moab, Utah and resorts to desperate measures in order to survive", "disappointed", "movie"));
        disappointedMovies.add(new Recommendation("Patch Adams", "The true story of a man who became a doctor and dedicated his methods to finding personal connection with his patients", "disappointed", "movie"));
        MOVIES_BY_MOOD.put("disappointed", disappointedMovies);

        // Anxious movies
        List<Recommendation> anxiousMovies = new ArrayList<>();
        anxiousMovies.add(new Recommendation("Amelie", "A whimsical romantic comedy about a shy waitress who decides to change the lives of those around her for the better", "anxious", "movie"));
        anxiousMovies.add(new Recommendation("The Secret Life of Walter Mitty", "A daydreamer escapes his anonymous life by disappearing into a world of fantasies filled with heroism, romance, and action", "anxious", "movie"));
        anxiousMovies.add(new Recommendation("Her", "In a near future, a lonely writer develops an unlikely relationship with an operating system designed to meet his every need", "anxious", "movie"));
        anxiousMovies.add(new Recommendation("Good Will Hunting", "A janitor at MIT has a gift for mathematics but needs help from a psychologist to find direction in his life", "anxious", "movie"));
        anxiousMovies.add(new Recommendation("Lost in Translation", "A faded movie star and a neglected young woman form an unlikely bond after crossing paths in Tokyo", "anxious", "movie"));
        // Adding more calming movies for anxious
        anxiousMovies.add(new Recommendation("Chef", "A chef who loses his restaurant job starts up a food truck in an effort to reclaim his creative promise", "anxious", "movie"));
        anxiousMovies.add(new Recommendation("About Time", "At the age of 21, Tim discovers he can travel in time and change what happens in his own life", "anxious", "movie"));
        anxiousMovies.add(new Recommendation("The Way Way Back", "Shy 14-year-old Duncan goes on summer vacation with his mother, her overbearing boyfriend, and her boyfriend's daughter", "anxious", "movie"));
        anxiousMovies.add(new Recommendation("Little Women", "The March sisters live and grow in post-Civil War America", "anxious", "movie"));
        anxiousMovies.add(new Recommendation("Groundhog Day", "A weatherman finds himself inexplicably living the same day over and over again", "anxious", "movie"));
        MOVIES_BY_MOOD.put("anxious", anxiousMovies);

        // Angry movies
        List<Recommendation> angryMovies = new ArrayList<>();
        angryMovies.add(new Recommendation("The Sound of Music", "A woman leaves an Austrian convent to become a governess to the children of a Naval officer widower", "angry", "movie"));
        angryMovies.add(new Recommendation("Soul", "A musician who has lost his passion for music is transported out of his body and must find his way back with the help of an infant soul learning about herself", "angry", "movie"));
        angryMovies.add(new Recommendation("The Lion King", "Lion prince Simba and his father are targeted by his bitter uncle, who wants to ascend the throne himself", "angry", "movie"));
        angryMovies.add(new Recommendation("Finding Nemo", "After his son is captured in the Great Barrier Reef and taken to Sydney, a timid clownfish sets out on a journey to bring him home", "angry", "movie"));
        angryMovies.add(new Recommendation("Coco", "Aspiring musician Miguel is confronted with his family's ancestral ban on music, enters the Land of the Dead to find his great-great-grandfather", "angry", "movie"));
        // Adding more calming movies for angry
        angryMovies.add(new Recommendation("The Holiday", "Two women troubled with guy-problems swap homes in each other's countries, where they each meet a local guy and fall in love", "angry", "movie"));
        angryMovies.add(new Recommendation("The Hundred-Foot Journey", "The Kadam family leaves India for France where they open a restaurant directly across the road from Madame Mallory's Michelin-starred restaurant", "angry", "movie"));
        angryMovies.add(new Recommendation("Field of Dreams", "Iowa farmer Ray builds a baseball field in his cornfield after hearing voices", "angry", "movie"));
        angryMovies.add(new Recommendation("Big Hero 6", "A special bond develops between plus-sized inflatable robot Baymax and prodigy Hiro Hamada", "angry", "movie"));
        angryMovies.add(new Recommendation("Mrs. Doubtfire", "After a bitter divorce, an actor disguises himself as a female housekeeper to spend time with his children", "angry", "movie"));
        MOVIES_BY_MOOD.put("angry", angryMovies);

        // Bored movies
        List<Recommendation> boredMovies = new ArrayList<>();
        boredMovies.add(new Recommendation("Inception", "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.", "bored", "movie"));
        boredMovies.add(new Recommendation("The Matrix", "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers", "bored", "movie"));
        boredMovies.add(new Recommendation("Interstellar", "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival", "bored", "movie"));
        boredMovies.add(new Recommendation("The Lord of the Rings: The Fellowship of the Ring", "A meek Hobbit from the Shire and eight companions set out on a journey to destroy the powerful One Ring and save Middle-earth from the Dark Lord Sauron", "bored", "movie"));
        boredMovies.add(new Recommendation("Pirates of the Caribbean: The Curse of the Black Pearl", "Blacksmith Will Turner teams up with eccentric pirate Captain Jack Sparrow to save his love from Jack's former pirate allies", "bored", "movie"));
        boredMovies.add(new Recommendation("Baby Driver", "After being coerced into working for a crime boss, a young getaway driver finds himself taking part in a heist doomed to fail", "bored", "movie"));
        boredMovies.add(new Recommendation("Ready Player One", "When the creator of a virtual reality called the OASIS dies, he makes a posthumous challenge to players to find his Easter Egg, which will give the finder his fortune", "bored", "movie"));
        boredMovies.add(new Recommendation("Thor: Ragnarok", "Imprisoned on the planet Sakaar, Thor must race against time to return to Asgard and stop Ragnarök", "bored", "movie"));
        boredMovies.add(new Recommendation("Kingsman: The Secret Service", "A spy organization recruits a promising street kid into the agency's training program, while a global threat emerges from a twisted tech genius", "bored", "movie"));
        boredMovies.add(new Recommendation("Jumanji: Welcome to the Jungle", "Four teenagers are sucked into a magical video game, and the only way they can escape is to work together to finish the game", "bored", "movie"));
        MOVIES_BY_MOOD.put("bored", boredMovies);

        // Nostalgic movies
        List<Recommendation> nostalgicMovies = new ArrayList<>();
        nostalgicMovies.add(new Recommendation("The Breakfast Club", "Five high school students meet in Saturday detention and discover how they have a lot more in common than they thought", "nostalgic", "movie"));
        nostalgicMovies.add(new Recommendation("Back to the Future", "Marty McFly, a 17-year-old high school student, is accidentally sent thirty years into the past in a time-traveling DeLorean", "nostalgic", "movie"));
        nostalgicMovies.add(new Recommendation("Stand By Me", "After the death of one of his friends, a writer recounts a childhood journey with his friends to find the body of a missing boy", "nostalgic", "movie"));
        nostalgicMovies.add(new Recommendation("E.T. the Extra-Terrestrial", "A troubled child summons the courage to help a friendly alien escape Earth and return to his home world", "nostalgic", "movie"));
        nostalgicMovies.add(new Recommendation("The Goonies", "A group of young misfits called The Goonies discover an ancient map and set out on an adventure to find a legendary pirate's long-lost treasure", "nostalgic", "movie"));
        nostalgicMovies.add(new Recommendation("Ferris Bueller's Day Off", "A high school wise guy is determined to have a day off from school, despite what the Principal thinks", "nostalgic", "movie"));
        nostalgicMovies.add(new Recommendation("Dirty Dancing", "Spending the summer at a Catskills resort with her family, Frances 'Baby' Houseman falls in love with the camp's dance instructor", "nostalgic", "movie"));
        nostalgicMovies.add(new Recommendation("Pretty Woman", "A man in a legal but hurtful business needs an escort for some social events, and hires a beautiful prostitute he meets... only to fall in love", "nostalgic", "movie"));
        nostalgicMovies.add(new Recommendation("Jurassic Park", "A pragmatic paleontologist visiting an almost complete theme park is tasked with protecting a couple of kids after a power failure causes the park's cloned dinosaurs to run loose", "nostalgic", "movie"));
        nostalgicMovies.add(new Recommendation("Home Alone", "An eight-year-old troublemaker must protect his house from a pair of burglars when he is accidentally left home alone by his family during Christmas vacation", "nostalgic", "movie"));
        MOVIES_BY_MOOD.put("nostalgic", nostalgicMovies);

        // Hopeful movies
        List<Recommendation> hopefulMovies = new ArrayList<>();
        hopefulMovies.add(new Recommendation("The Shawshank Redemption", "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency", "hopeful", "movie"));
        hopefulMovies.add(new Recommendation("It's a Wonderful Life", "An angel is sent from Heaven to help a desperately frustrated businessman by showing him what life would have been like if he had never existed", "hopeful", "movie"));
        hopefulMovies.add(new Recommendation("The Pursuit of Happyness", "A struggling salesman takes custody of his son as he's poised to begin a life-changing professional career", "hopeful", "movie"));
        hopefulMovies.add(new Recommendation("WALL-E", "In a distant future, a small waste-collecting robot inadvertently embarks on a space journey that will ultimately decide the fate of mankind", "hopeful", "movie"));
        hopefulMovies.add(new Recommendation("Slumdog Millionaire", "A Mumbai teenager reflects on his life after being accused of cheating on the Indian version of 'Who Wants to be a Millionaire?'", "hopeful", "movie"));
        hopefulMovies.add(new Recommendation("Billy Elliot", "A talented young boy becomes torn between his unexpected love of dance and the disintegration of his family", "hopeful", "movie"));
        hopefulMovies.add(new Recommendation("Life of Pi", "A young man who survives a disaster at sea is hurtled into an epic journey of adventure and discovery", "hopeful", "movie"));
        hopefulMovies.add(new Recommendation("Dead Poets Society", "Maverick teacher John Keating uses poetry to embolden his boarding school students to new heights of self-expression", "hopeful", "movie"));
        hopefulMovies.add(new Recommendation("Little Women", "The March sisters live and grow in post-Civil War America", "hopeful", "movie"));
        hopefulMovies.add(new Recommendation("The Theory of Everything", "A look at the relationship between the famous physicist Stephen Hawking and his wife", "hopeful", "movie"));
        MOVIES_BY_MOOD.put("hopeful", hopefulMovies);

        // Stressed movies
        List<Recommendation> stressedMovies = new ArrayList<>();
        stressedMovies.add(new Recommendation("The Princess Diaries", "Mia Thermopolis has just found out that she is the heir apparent to the throne of Genovia", "stressed", "movie"));
        stressedMovies.add(new Recommendation("Ferris Bueller's Day Off", "A high school wise guy is determined to have a day off from school, despite what the Principal thinks", "stressed", "movie"));
        stressedMovies.add(new Recommendation("Finding Nemo", "After his son is captured in the Great Barrier Reef and taken to Sydney, a timid clownfish sets out on a journey to bring him home", "stressed", "movie"));
        stressedMovies.add(new Recommendation("Paddington", "A young Peruvian bear travels to London in search of a home", "stressed", "movie"));
        stressedMovies.add(new Recommendation("The Secret Life of Pets", "The quiet life of a terrier named Max is upended when his owner takes in Duke, a stray whom Max instantly dislikes", "stressed", "movie"));
        stressedMovies.add(new Recommendation("Legally Blonde", "Elle Woods, a fashionable sorority queen, is dumped by her boyfriend. She decides to follow him to law school", "stressed", "movie"));
        stressedMovies.add(new Recommendation("Napoleon Dynamite", "A listless and alienated teenager decides to help his new friend win the class presidency", "stressed", "movie"));
        stressedMovies.add(new Recommendation("Monsters, Inc.", "In order to power the city, monsters have to scare children so that they scream", "stressed", "movie"));
        stressedMovies.add(new Recommendation("The Emperor's New Groove", "Emperor Kuzco is turned into a llama by his ex-administrator Yzma", "stressed", "movie"));
        stressedMovies.add(new Recommendation("Enchanted", "A young maiden in a land called Andalasia, who is prepared to be wed, is sent away to New York City by an evil Queen", "stressed", "movie"));
        MOVIES_BY_MOOD.put("stressed", stressedMovies);

        // Curious movies
        List<Recommendation> curiousMovies = new ArrayList<>();
        curiousMovies.add(new Recommendation("Inception", "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.", "curious", "movie"));
        curiousMovies.add(new Recommendation("The Prestige", "After a tragic accident, two stage magicians engage in a battle to create the ultimate illusion", "curious", "movie"));
        curiousMovies.add(new Recommendation("Memento", "A man with short-term memory loss attempts to track down his wife's murderer", "curious", "movie"));
        curiousMovies.add(new Recommendation("Shutter Island", "In 1954, a U.S. Marshal investigates the disappearance of a murderer who escaped from a hospital for the criminally insane", "curious", "movie"));
        curiousMovies.add(new Recommendation("Ex Machina", "A young programmer is selected to participate in a ground-breaking experiment in synthetic intelligence", "curious", "movie"));
        curiousMovies.add(new Recommendation("Arrival", "A linguist works with the military to communicate with alien lifeforms after twelve mysterious spacecraft appear around the world", "curious", "movie"));
        curiousMovies.add(new Recommendation("The Truman Show", "An insurance salesman discovers his entire life is actually a reality TV show", "curious", "movie"));
        curiousMovies.add(new Recommendation("Eternal Sunshine of the Spotless Mind", "When their relationship turns sour, a couple undergoes a medical procedure to have each other erased from their memories", "curious", "movie"));
        curiousMovies.add(new Recommendation("Donnie Darko", "A troubled teenager is plagued by visions of a man in a large rabbit suit who manipulates him to commit a series of crimes", "curious", "movie"));
        curiousMovies.add(new Recommendation("The Sixth Sense", "A boy who communicates with spirits seeks the help of a disheartened child psychologist", "curious", "movie"));
        MOVIES_BY_MOOD.put("curious", curiousMovies);
    }
    
    /**
     * Initialize fallback music recommendations
     */
    private static void initializeMusic() {
        // Happy music
        List<Recommendation> happyMusic = new ArrayList<>();
        happyMusic.add(new Recommendation("Happy", "Artist: Pharrell Williams | Album: G I R L", "happy", "music"));
        happyMusic.add(new Recommendation("Uptown Funk", "Artist: Mark Ronson ft. Bruno Mars | Album: Uptown Special", "happy", "music"));
        happyMusic.add(new Recommendation("Can't Stop the Feeling!", "Artist: Justin Timberlake | Album: Trolls (Original Motion Picture Soundtrack)", "happy", "music"));
        happyMusic.add(new Recommendation("Good as Hell", "Artist: Lizzo | Album: Cuz I Love You", "happy", "music"));
        happyMusic.add(new Recommendation("Walking on Sunshine", "Artist: Katrina and the Waves | Album: Walking on Sunshine", "happy", "music"));
        MUSIC_BY_MOOD.put("happy", happyMusic);
        
        // Sad music
        List<Recommendation> sadMusic = new ArrayList<>();
        sadMusic.add(new Recommendation("Someone Like You", "Artist: Adele | Album: 21", "sad", "music"));
        sadMusic.add(new Recommendation("Fix You", "Artist: Coldplay | Album: X&Y", "sad", "music"));
        sadMusic.add(new Recommendation("Skinny Love", "Artist: Bon Iver | Album: For Emma, Forever Ago", "sad", "music"));
        sadMusic.add(new Recommendation("Hurt", "Artist: Johnny Cash | Album: American IV: The Man Comes Around", "sad", "music"));
        sadMusic.add(new Recommendation("All I Want", "Artist: Kodaline | Album: In a Perfect World", "sad", "music"));
        MUSIC_BY_MOOD.put("sad", sadMusic);
        
        // Excited music
        List<Recommendation> excitedMusic = new ArrayList<>();
        excitedMusic.add(new Recommendation("Don't Stop Me Now", "Artist: Queen | Album: Jazz", "excited", "music"));
        excitedMusic.add(new Recommendation("Levels", "Artist: Avicii | Album: True", "excited", "music"));
        excitedMusic.add(new Recommendation("Titanium", "Artist: David Guetta ft. Sia | Album: Nothing but the Beat", "excited", "music"));
        excitedMusic.add(new Recommendation("Wake Me Up", "Artist: Avicii | Album: True", "excited", "music"));
        excitedMusic.add(new Recommendation("Can't Hold Us", "Artist: Macklemore & Ryan Lewis | Album: The Heist", "excited", "music"));
        MUSIC_BY_MOOD.put("excited", excitedMusic);
        
        // Scared music
        List<Recommendation> scaredMusic = new ArrayList<>();
        scaredMusic.add(new Recommendation("Weightless", "Artist: Marconi Union | Album: Weightless", "scared", "music"));
        scaredMusic.add(new Recommendation("Claire de Lune", "Artist: Claude Debussy | Album: Classical Essentials", "scared", "music"));
        scaredMusic.add(new Recommendation("A Quiet Thought", "Artist: Yiruma | Album: First Love", "scared", "music"));
        scaredMusic.add(new Recommendation("Experience", "Artist: Ludovico Einaudi | Album: In a Time Lapse", "scared", "music"));
        scaredMusic.add(new Recommendation("Nocturne Op. 9 No. 2", "Artist: Frédéric Chopin | Album: Nocturnes", "scared", "music"));
        MUSIC_BY_MOOD.put("scared", scaredMusic);
        
        // Disappointed music
        List<Recommendation> disappointedMusic = new ArrayList<>();
        disappointedMusic.add(new Recommendation("Rise Up", "Artist: Andra Day | Album: Cheers to the Fall", "disappointed", "music"));
        disappointedMusic.add(new Recommendation("Fight Song", "Artist: Rachel Platten | Album: Wildfire", "disappointed", "music"));
        disappointedMusic.add(new Recommendation("Believer", "Artist: Imagine Dragons | Album: Evolve", "disappointed", "music"));
        disappointedMusic.add(new Recommendation("Stronger", "Artist: Kelly Clarkson | Album: Stronger", "disappointed", "music"));
        disappointedMusic.add(new Recommendation("Roar", "Artist: Katy Perry | Album: Prism", "disappointed", "music"));
        MUSIC_BY_MOOD.put("disappointed", disappointedMusic);

        // Anxious music
        List<Recommendation> anxiousMusic = new ArrayList<>();
        anxiousMusic.add(new Recommendation("Breathe Me", "Artist: Sia | Album: Colour The Small One", "anxious", "music"));
        anxiousMusic.add(new Recommendation("Weightless", "Artist: Marconi Union | Album: Marconi Union", "anxious", "music"));
        anxiousMusic.add(new Recommendation("Gymnopédie No.1", "Artist: Erik Satie | Album: Piano Works", "anxious", "music"));
        anxiousMusic.add(new Recommendation("Holocene", "Artist: Bon Iver | Album: Bon Iver", "anxious", "music"));
        anxiousMusic.add(new Recommendation("Bloom", "Artist: The Paper Kites | Album: twelvefour", "anxious", "music"));
        MUSIC_BY_MOOD.put("anxious", anxiousMusic);

        // Angry music
        List<Recommendation> angryMusic = new ArrayList<>();
        angryMusic.add(new Recommendation("Keep Your Head Up", "Artist: Andy Grammer | Album: Andy Grammer", "angry", "music"));
        angryMusic.add(new Recommendation("Shake It Off", "Artist: Taylor Swift | Album: 1989", "angry", "music"));
        angryMusic.add(new Recommendation("Everything's Gonna Be Alright", "Artist: Bob Marley | Album: Legend", "angry", "music"));
        angryMusic.add(new Recommendation("Three Little Birds", "Artist: Bob Marley | Album: Exodus", "angry", "music"));
        angryMusic.add(new Recommendation("Don't Worry Be Happy", "Artist: Bobby McFerrin | Album: Simple Pleasures", "angry", "music"));
        MUSIC_BY_MOOD.put("angry", angryMusic);

        // Bored music
        List<Recommendation> boredMusic = new ArrayList<>();
        boredMusic.add(new Recommendation("Dance Monkey", "Artist: Tones and I | Album: The Kids Are Coming", "bored", "music"));
        boredMusic.add(new Recommendation("Blinding Lights", "Artist: The Weeknd | Album: After Hours", "bored", "music"));
        boredMusic.add(new Recommendation("Savage Love", "Artist: Jason Derulo | Album: Savage Love", "bored", "music"));
        boredMusic.add(new Recommendation("Dynamite", "Artist: BTS | Album: BE", "bored", "music"));
        boredMusic.add(new Recommendation("Bad Guy", "Artist: Billie Eilish | Album: When We All Fall Asleep, Where Do We Go?", "bored", "music"));
        MUSIC_BY_MOOD.put("bored", boredMusic);
    }
    
    /**
     * Initialize fallback book recommendations
     */
    private static void initializeBooks() {
        // Happy books
        List<Recommendation> happyBooks = new ArrayList<>();
        happyBooks.add(new Recommendation("The Rosie Project", "A socially awkward genetics professor sets out to find the perfect wife using scientific methods", "happy", "book"));
        happyBooks.add(new Recommendation("Where'd You Go, Bernadette", "When her anxiety-ridden mother disappears, 15-year-old Bee embarks on a journey to find her", "happy", "book"));
        happyBooks.add(new Recommendation("Crazy Rich Asians", "When Rachel Chu agrees to spend the summer in Singapore with her boyfriend, she envisions a humble family home. She has no idea what's coming", "happy", "book"));
        happyBooks.add(new Recommendation("A Man Called Ove", "A curmudgeon hides a terrible personal loss beneath a cranky and short-tempered exterior while clashing with new neighbors", "happy", "book"));
        happyBooks.add(new Recommendation("The Hitchhiker's Guide to the Galaxy", "Seconds before Earth is demolished to make way for a galactic freeway, Arthur Dent is rescued by his friend Ford Prefect", "happy", "book"));
        BOOKS_BY_MOOD.put("happy", happyBooks);
        
        // Sad books
        List<Recommendation> sadBooks = new ArrayList<>();
        sadBooks.add(new Recommendation("When Breath Becomes Air", "A memoir about Paul Kalanithi's life and battle with metastatic lung cancer", "sad", "book"));
        sadBooks.add(new Recommendation("Man's Search for Meaning", "Viktor Frankl's memoir about his time in Nazi concentration camps and the importance of finding purpose in life", "sad", "book"));
        sadBooks.add(new Recommendation("The Alchemist", "A story about an Andalusian shepherd boy following his dreams and discovering his personal legend", "sad", "book"));
        sadBooks.add(new Recommendation("Tuesdays with Morrie", "An old man, a young man, and life's greatest lesson", "sad", "book"));
        sadBooks.add(new Recommendation("Eat Pray Love", "A memoir chronicling the author's trip around the world after her divorce", "sad", "book"));
        BOOKS_BY_MOOD.put("sad", sadBooks);
        
        // Add more fallback books for other moods
        // ...
    }
    
    /**
     * Get fallback movie recommendations for a specific mood
     */
    public static List<Recommendation> getMovieRecommendations(String mood) {
        List<Recommendation> fallbackList = MOVIES_BY_MOOD.get(mood.toLowerCase());
        return fallbackList != null ? fallbackList : new ArrayList<>();
    }
    
    /**
     * Get fallback movie recommendations for multiple moods
     */
    public static List<Recommendation> getMovieRecommendations(List<String> moods) {
        if (moods == null || moods.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Try to get recommendations for the first mood in the list
        for (String mood : moods) {
            List<Recommendation> recommendations = MOVIES_BY_MOOD.get(mood.toLowerCase());
            if (recommendations != null && !recommendations.isEmpty()) {
                // Set the mood field to include all selected moods
                String allMoods = String.join(", ", moods);
                for (Recommendation rec : recommendations) {
                    // Create a new recommendation with updated mood
                    rec = new Recommendation(rec.getTitle(), rec.getDescription(), allMoods, "movie");
                }
                return recommendations;
            }
        }
        
        // If no recommendations found for any mood, return empty list
        return new ArrayList<>();
    }
    
    /**
     * Get fallback music recommendations for a specific mood
     */
    public static List<Recommendation> getMusicRecommendations(String mood) {
        List<Recommendation> fallbackList = MUSIC_BY_MOOD.get(mood.toLowerCase());
        return fallbackList != null ? fallbackList : new ArrayList<>();
    }
    
    /**
     * Get fallback music recommendations for multiple moods
     */
    public static List<Recommendation> getMusicRecommendations(List<String> moods) {
        if (moods == null || moods.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Try to get recommendations for the first mood in the list
        for (String mood : moods) {
            List<Recommendation> recommendations = MUSIC_BY_MOOD.get(mood.toLowerCase());
            if (recommendations != null && !recommendations.isEmpty()) {
                // Set the mood field to include all selected moods
                String allMoods = String.join(", ", moods);
                for (Recommendation rec : recommendations) {
                    // Create a new recommendation with updated mood
                    rec = new Recommendation(rec.getTitle(), rec.getDescription(), allMoods, "music");
                }
                return recommendations;
            }
        }
        
        // If no recommendations found for any mood, return empty list
        return new ArrayList<>();
    }
    
    /**
     * Get fallback book recommendations for a specific mood
     */
    public static List<Recommendation> getBookRecommendations(String mood) {
        List<Recommendation> fallbackList = BOOKS_BY_MOOD.get(mood.toLowerCase());
        return fallbackList != null ? fallbackList : new ArrayList<>();
    }
    
    /**
     * Get fallback book recommendations for multiple moods
     */
    public static List<Recommendation> getBookRecommendations(List<String> moods) {
        if (moods == null || moods.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Try to get recommendations for the first mood in the list
        for (String mood : moods) {
            List<Recommendation> recommendations = BOOKS_BY_MOOD.get(mood.toLowerCase());
            if (recommendations != null && !recommendations.isEmpty()) {
                // Set the mood field to include all selected moods
                String allMoods = String.join(", ", moods);
                for (Recommendation rec : recommendations) {
                    // Create a new recommendation with updated mood
                    rec = new Recommendation(rec.getTitle(), rec.getDescription(), allMoods, "book");
                }
                return recommendations;
            }
        }
        
        // If no recommendations found for any mood, return empty list
        return new ArrayList<>();
    }
} 