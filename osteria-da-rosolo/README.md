# Osteria da Rosolo — Sito Dimostrativo

Sito vetrina realizzato come **proposta commerciale** per l'Osteria da Rosolo di Lucca (Corte Campana, 3), che al momento non ha un sito proprio funzionante. Obiettivo: mostrare ai titolari come potrebbe apparire il loro sito, per proporne la realizzazione/vendita.

## Come vederlo
Il sito è statico (HTML/CSS/JS puro, nessuna build richiesta). Basta aprire `index.html` in un browser, oppure servirlo localmente:

```bash
cd osteria-da-rosolo
python3 -m http.server 8080
# poi apri http://localhost:8080
```

## Contenuti e fonti
Le informazioni sono state raccolte da fonti pubbliche (Tripadvisor, Google, Yelp, Foursquare, RestaurantGuru, Zonzofox, PagineGialle) perché il locale non ha un sito ufficiale attivo (il dominio osteriadarosolo.com risulta registrato ma irraggiungibile/non funzionante al momento della raccolta). **Da verificare con i titolari prima della pubblicazione:**

- **Menù e prezzi**: i piatti indicati (pappardelle al cinghiale, tordelli alla lucchese, maltagliati al branzino, ecc.) sono ricorrenti nelle recensioni, ma i prezzi in pagina sono **indicativi** — vanno confermati o forniti dal ristorante.
- **Orari**: raccolti da un aggregatore, potrebbero essere cambiati. Verificare Lun–Dom e giorno di chiusura (risulta il giovedì).
- **Foto**: tutte le immagini sono foto stock (Unsplash) puramente dimostrative, per mostrare stile e impaginazione. **Vanno sostituite con fotografie reali** del locale, del cortile di Corte Campana e dei piatti — idealmente con un servizio fotografico professionale, punto di forza da vendere insieme al sito.
- **Recensioni**: le citazioni in home sono parafrasi dei temi ricorrenti nelle recensioni online (non citazioni testuali attribuite a persone reali), da sostituire con vere recensioni con permesso, o widget ufficiali Google/Tripadvisor.
- **Social/telefono**: numero di telefono e pagina Facebook trovati pubblicamente; da confermare che siano ancora quelli corretti.

## Struttura
```
osteria-da-rosolo/
├── index.html      # tutte le sezioni del sito (one-page)
├── css/style.css   # stile e layout responsive
├── js/script.js    # menu mobile, banner "bozza", piccoli dettagli
└── images/         # cartella pronta per le foto reali del locale
```

## Sezioni del sito
1. **Hero** — nome, tagline, valutazione media, CTA
2. **La Storia** — conduzione familiare, atmosfera del cortile
3. **Menù** — antipasti, primi, secondi, dolci e cantina
4. **Galleria** — placeholder fotografico
5. **Recensioni** — riepilogo reputazione online
6. **Dove Siamo** — indirizzo, orari, mappa incorporata, no prenotazioni
7. **Contatti** — telefono e social, footer

## Prossimi passi per la vendita
- Sostituire le foto stock con scatti reali (proporsi anche per il servizio fotografico).
- Far validare menù, prezzi e orari dal ristorante.
- Aggiungere eventuale sistema di recensioni/social feed collegato a Google e Tripadvisor.
- Valutare dominio e hosting (es. riattivare/redirect da osteriadarosolo.com) e una casella email dedicata.
