
extensions [matrix]

globals [materialtolandfill moneystart moneyend interestrate CO2-tax contractrate ROCS-incentive]
  

breed [ companies company ]
breed [ foodprocessors foodprocessor ]
breed [ externalworld externalworlds ]
undirected-link-breed [ friends friend ]
directed-link-breed [ contracts contract ]

companies-own [materialmaxin materialin materialneed materialout materialinprofile process contractlength moneyold money profit ethos unitrawmaterialcost unitproductvalue operatingcosts operatingvolume initialloan loan age]
foodprocessors-own [materialmaxin materialin materialneed materialout materialinprofile process moneyold money profit ethos unitcontractvalue unitproductvalue priceincrease pricedecrease]
contracts-own [materialtype lengthofcontract amountofcontract valueofcontract]
; contractlength is how long contracts are.
;materialmaxin is the amount of material that companies can take in. 
;materialin specifies the total amount of material the company takes in.
;materialneed is the difference between what the company currently takes in and what their maximum capacity is. 
;materialout is the amount of material that a company produces. 
;materialinprofile is the input profile of a company
;process is a matrix that specifies how the inputs get converted to an output.
;unitcontractvalue is the amount that the materialout can be sold to a company for - a negative value represents 
; a charge.  The unitcontractvalue is adjusted up or down depending on whether a food processor can sell or not
; sell their waste.
;unitproductvalue is the amount that the materialout is sold for to the outside world or landfill.
;
;amountofcontract is the number of tonnes/day that is transferred under the contract.
;valueofcontract is the total value of the contract/day = number of tonnes * unit value of product.

; set up initial values for variables.
to setup
 
  __clear-all-and-reset-ticks
  ask patches [set pcolor white]
  set moneystart 0
  set moneyend 0
  set interestrate 0.05
  set CO2-tax 0
  set contractrate 5
  set materialtolandfill matrix:from-row-list [ [0] [0] [0] [0] [0] ]
  set-default-shape foodprocessors "circle"
  set-default-shape companies "circle"
  
  set ROCS-incentive 14
  let price-solid 7.5
  let price-oil 40
  let number-of-composters 20
  let number-of-AD 20
  let sign-C -1 ;; sign of composter profit
;;  let sign-AD -1 ;; sign of composter profit 
  let initial-money-C 20000
  let initial-money-AD 20000
  
  create-foodprocessors number-of-wasteproducers 
; Food processors
  ask foodprocessors [
    set materialmaxin 1000 + random 5900
    set size materialmaxin / 6000
    set materialinprofile matrix:from-row-list [[1] [0] [0] [0] [0]]
    set materialin matrix:times-scalar materialinprofile materialmaxin
    set materialneed matrix:times-scalar materialinprofile materialmaxin
    set process matrix:from-row-list [ [0.925 0 0 0 0] [0.072 0 0 0 0] [0.003 0 0 0 0] [0 0 0 0 0] [0 0 0 0 0]] 
    set unitcontractvalue matrix:from-row-list [[10] [-41] [600] [0] [0]]
    set unitproductvalue matrix:from-row-list [[20 ] [ -64 ] [ -64 ] [0] [0]]
    matrix:set unitproductvalue 1 0 ( price-solid )
    matrix:set unitproductvalue 2 0 ( price-oil )
    set priceincrease matrix:from-row-list [[0] [0.10] [0.10] [0] [0]]
    set pricedecrease matrix:from-row-list [[0] [0.05] [0.05] [0] [0]]
    
    set money 417998 * materialmaxin - 77600000
    set moneyold money
    set profit 8.85 * materialmaxin - 385.0
    
    set color green
    let xval random -19
    let yval random 19
    setxy xval yval    
  ]
  create-externalworld 1
  ask externalworld [
    set color green
    set size 4
    set shape "square"
    setxy 18 0  
  ]
  
  create-companies number-of-composters
  ask companies [
        set materialmaxin random 200
        set size materialmaxin / 200
        set materialinprofile matrix:from-row-list [[0] [1] [0] [0] [0]]
        set materialin matrix:from-row-list [ [0] [0] [0] [0] [0] ]
        set materialneed matrix:times-scalar materialinprofile materialmaxin

        set process matrix:from-row-list [ [0 0.55 0 0 0] [0 0 0 0 0] [0 0 0 0 0] [0 0.324 0 0 0] [0 0 0 0 0]]
        set unitrawmaterialcost 0 
        set unitproductvalue matrix:from-row-list [[20] [0] [0] [-1] [0]] 
        matrix:set unitproductvalue 3 0 ( - CO2-tax )
        set color black
        set operatingcosts 5 * materialmaxin 
        set initialloan 500 * materialmaxin
        set loan initialloan
        
        set money initialloan + initial-money-C
        set moneyold money
        set profit sign * materialmaxin / 4.6
        set contractlength random 4
        
        set age 0
        setxy 5 -12
        let xval random -12 - 5
        let yval random -12 - 5
        setxy xval yval
        create-friends-with n-of number-of-wasteproducers foodprocessors ;; friends with everyone
        ask friends [ set color red ]
  ]
  
  create-companies number-of-AD
  ask companies with [color != black] [
        set materialmaxin 150
        set size materialmaxin / 200
        set materialinprofile matrix:from-row-list [[0] [1] [0] [0] [0]]
        set materialneed matrix:times-scalar materialinprofile materialmaxin
        set materialin matrix:from-row-list [ [0] [0] [0] [0] [0] ]
        set process matrix:from-row-list [ [0 1 0 0 0] [0 0 0 0 0] [0 0 0 0 0] [0 1.4 0 0 0] [0 0 0 0 0] ] 
        set unitrawmaterialcost 0 
        set unitproductvalue matrix:from-row-list [[1] [0] [0] [0] [0]]
        matrix:set unitproductvalue 0 0 ( 9.6 * ROCs-incentive + 0.04 * 50)
        matrix:set unitproductvalue 3 0 ( -1 * CO2-tax )
        set operatingcosts 80 * materialmaxin
        set initialloan 50000 * materialmaxin
        set loan initialloan
  
        set money ( loan - 680000 ) / 0.09
        set moneyold money
        set profit 0.000085 * money - 7726.0
        set contractlength 3.0 * profit -7726.0 ;;random 180

        set color red
        set age 0
        let xval random 12 + 5
        let yval random -12 - 5
        setxy xval yval
        create-friends-with n-of initial-contacts foodprocessors
        ask friends [ set color red ]
  ]
  
  
  reset-ticks  
end

to setupOLD
  ;; (for this model to work with NetLogo's new plotting features,
  ;; __clear-all-and-reset-ticks should be replaced with clear-all at
  ;; the beginning of your setup procedure and reset-ticks at the end
  ;; of the procedure.)
  __clear-all-and-reset-ticks
  ask patches [set pcolor white]
  set moneystart 0
  set moneyend 0
  set interestrate 0.05
  set CO2-tax 0
  set contractrate 5
  set materialtolandfill matrix:from-row-list [ [0] [0] [0] [0] [0] ]
  set-default-shape foodprocessors "circle"
  set-default-shape companies "circle"
  set ROCS-incentive 14
  create-foodprocessors number-of-wasteproducers 
; Food processors
  ask foodprocessors [
    set materialmaxin 1000 + random 5900
    set size materialmaxin / 6000
    set materialinprofile matrix:from-row-list [[1] [0] [0] [0] [0]]
    set materialin matrix:times-scalar materialinprofile materialmaxin
    set materialneed matrix:times-scalar materialinprofile materialmaxin
    set process matrix:from-row-list [ [0.925 0 0 0 0] [0.072 0 0 0 0] [0.003 0 0 0 0] [0 0 0 0 0] [0 0 0 0 0]] 
    set unitcontractvalue matrix:from-row-list [[10] [-41] [600] [0] [0]]
    set unitproductvalue matrix:from-row-list [[20 ] [ -64 ] [ -64 ] [0] [0]]
    matrix:set unitproductvalue 1 0 ( - landfillprice )
    matrix:set unitproductvalue 2 0 ( - landfillprice )
    set priceincrease matrix:from-row-list [[0] [0.10] [0.10] [0] [0]]
    set pricedecrease matrix:from-row-list [[0] [0.05] [0.05] [0] [0]]
    set money 100
    set moneyold money
    set profit 0
    set color green
    let xval random -19
    let yval random 19
    setxy xval yval    
  ]
  create-externalworld 1
  ask externalworld [
    set color green
    set size 4
    set shape "square"
    setxy 18 0  
  ]
  reset-ticks  
;  movie-start "out1.mov"
;  movie-set-frame-rate 1
;  movie-grab-interface 
;  repeat 6000
;   [ go
;      if ticks mod 50 = 0 [ movie-grab-interface ]
;   ]
;   movie-close
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to make-movie
  movie-start "out2.mov"
  movie-set-frame-rate 1
;  movie-grab-interface 
  repeat 4000
   [ go
     if ticks mod 50 = 0 [ movie-grab-interface ]
   ]
   movie-close
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to go
  set ROCs-incentive ROs-incentive
;  count-initial-money
; Create waste.
  food-processors-buy-raw-materials
  ask foodprocessors [ flow-materials ]
; Supply waste to existing contracts
  ask contracts [ update-existing-contracts ]
; Set up 5 new contracts to try to get rid of the rest of the waste.  
  let n 0
    while [ n < contractrate ] [  
      ask companies [ setup-new-contracts ]
      set n n + 1
    ]
; Food processors now update their "selling" prices for waste.  The price goes down if they 
; cannot get rid of all their waste, goes up if they can.  Initially prices have been set so
; that they have to pay to get rid of solid waste, but get an income from waste oil.   
  ask foodprocessors [ update-selling-prices ]
; Finally food processors "sell" all their material ie the goods they have produced and
; any remaining waste.  They get an income form their produce
; and pay for everything else to go to landfill. 
  ask foodprocessors [ sell-materialout ]
; Companies that deal with waste buy any additional raw materials that they need.
  companies-buy-raw-materials
; Now the companies that deal with waste process the waste to produce outputs.  
  ask companies [ flow-materials ]
; Companies then sell their products.
  ask companies [ sell-materialout ]
; Companies then pay off any loans and pay any operating costs.
  ask companies [ update-finances ]
; Calculate profit
  ask companies [ calculate-profit ]
  ask foodprocessors [ calculate-profit ]
; If food processors or companies run out of money, then they go bust. 
  ask foodprocessors [ go-bust ]
  ask companies [ go-bust ]
  ask companies [ get-older ]
;  
  tick
  makeplots
; New friends are made 
  ask companies [ make-new-friends ]
; New companies are created if there is any waste that they can process.   
  create-new-companies
;  count-final-money
; Contract details are updated: they end if they have reached the end of their contract time.
  update-contract-details
; Prices that are set by the sliders (landfill price) are updated.  
  ask foodprocessors [ update-prices ]
  ask companies [ update-prices ]
;  ask companies with [color = red] [
;   matrix:set unitproductvalue 0 0 ( 9.6 * ROCs-incentive + 0.04 * 50)
; ]
; initialise variables for another loop  
  set materialtolandfill matrix:from-row-list [ [0] [0] [0] [0] [0] ]
  ask companies [
      set materialneed matrix:times-scalar materialinprofile materialmaxin
  ]
  set moneystart 0
  set moneyend 0
;  show mean [ matrix:get unitcontractvalue 1 0 ] of foodprocessors
;  show mean [ matrix:get unitcontractvalue 2 0 ] of foodprocessors

if ticks > 50000 [ stop] 
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;
to count-initial-money
  ask companies [
;    show money
    set moneystart moneystart + money
 ;   show moneystart
  ]
    ask foodprocessors [
;    show money
    set moneystart moneystart + money
 ;   show moneystart
  ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to count-final-money
  ask companies [
;    show money
    set moneyend moneyend + money
 ;   show moneystart
  ]
    ask foodprocessors [
;    show money
    set moneyend moneyend + money
 ;   show moneystart
  ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to food-processors-buy-raw-materials
  ask foodprocessors [
      let mcost 0
      set materialin matrix:times-scalar materialinprofile materialmaxin
      set mcost ( matrix:get materialin 0 0  * matrix:get unitcontractvalue 0 0 )
      set money money - mcost
  ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; flow-material describes the processing that the food processors/companies do.  
to flow-materials
  set materialout matrix:times process materialin
; Now set to zero the material in for M, S, L, G and W. 
   set materialin matrix:from-row-list [ [0] [0] [0] [0] [0] ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; supply existing contractors.  Update finances accordingly.
to update-existing-contracts
    let mtype materialtype
    let acontract amountofcontract
    let vcontract valueofcontract
; update supplier by reducing the amount of material they have left to sell and increasing the
; money they have.
    ask end1 [
      let newmaterialout matrix:get materialout mtype 0
      set newmaterialout newmaterialout - acontract
      matrix:set materialout mtype 0 newmaterialout
      set money money +  vcontract
    ]   
; update client by increasing the amount of material they have coming in and decreasing the
; money they have.
    ask end2 [
      let newmaterialin matrix:get materialin mtype 0
      set newmaterialin newmaterialin + acontract
      matrix:set materialin mtype 0 newmaterialin
      set money money - vcontract
    ] 
;  show materialtype
; show amountofcontract
; show valueofcontract
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; set-up-new-contracts first looks to see who has some material need.  For the
; companies that have a material need it looks to see who, in their friendship 
; network could supply that material need. The one who is selling at the lowest
; price is then chosen.
; A contract (link) is then established.  This link is assigned a length of time 
; and a material flow amount and value and the material variables of the ends of the link
; are updated.
to setup-new-contracts
;  show materialneed
  let p 1 
  while [p <= 3] [ 
    let myneed matrix:get materialneed p 0 - matrix:get materialin p 0
    let needprovided 0
    let needsatisfied 0
    let vcontract 0
    let lcontract 0
    set lcontract contractlength
    if myneed > 0 [
      let possiblepartners nobody
      let chosenpartner nobody
      set possiblepartners friend-neighbors with [ matrix:get materialout p 0 > 0 ]
      if any? possiblepartners [
;        set chosenpartner one-of possiblepartners
        set chosenpartner min-one-of possiblepartners [ matrix:get unitcontractvalue p 0 ]
        ask chosenpartner [ set needprovided matrix:get materialout p 0 ]
        ifelse needprovided > myneed [ set needsatisfied myneed 
        ]
        [ set needsatisfied needprovided 
        ]
        ask chosenpartner [ set vcontract matrix:get unitcontractvalue p 0 * needsatisfied ]
        create-contract-from chosenpartner [ set materialtype p
          ;                                       set lengthofcontract timeforcontract
          set lengthofcontract lcontract
          set amountofcontract needsatisfied
          set valueofcontract vcontract
          set thickness 0.2
          if p = 1 [ set color black ]
          if p = 2 [ set color blue ]
          if p = 3 [ set color green ]
;          show materialtype
;          show amountofcontract
;          show valueofcontract
        ] 
        ask chosenpartner [ matrix:set materialout p 0 needprovided - needsatisfied  
          set money money + vcontract
        ]
      ] 
      matrix:set materialneed p 0 myneed - needsatisfied
      let currentmaterialin matrix:get materialin p 0
      matrix:set materialin p 0 currentmaterialin + needsatisfied 
      set money money - vcontract
    ]
    set p p + 1
;    show materialout
;    show money 
;    show materialneed
;    show materialin
  ]
end  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; If food processors have no solid waste or waste oil left after supplying composters, AD and biodiesel then increase the
; price they are selling at.  If they have waste left, then reduce the price.
to update-selling-prices
  let p 1
  while [p <= 2][
    let wasteleft matrix:get materialout p 0
    let oldcontractvalue matrix:get unitcontractvalue p 0
    let priceincrement matrix:get priceincrease p 0
    let pricedecrement matrix:get pricedecrease p 0    
      ifelse wasteleft > 0 [ 
      let minprice ( - landfillprice )
        ifelse oldcontractvalue > minprice [   
        matrix:set unitcontractvalue p 0 oldcontractvalue - pricedecrement 
        ]
        [
        matrix:set unitcontractvalue p 0 minprice       
        ]
      ]
      [
      matrix:set unitcontractvalue p 0 oldcontractvalue + priceincrement 
      ]
    set p p + 1     
  ]
;  show unitcontractvalue
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 
; Sell any products that are produced. Any solid waste or waste oil that has not been sold
; is sent to landfill.  The amount of material sent to landfill is monitored.  It is assumed
; that all the material that flows out is either sold for profit of sent to landfill, so 
; materialout is set to zero at the end of this subroutine.
;
to sell-materialout
  let totalproductvalue matrix:times-element-wise materialout unitproductvalue
  let p 0
  while [p <= 4] [
    let pvalue matrix:get totalproductvalue p 0
    set money money + pvalue
    let newwaste matrix:get materialout p 0
    let oldwaste matrix:get materialtolandfill p 0
    matrix:set materialtolandfill p 0 newwaste + oldwaste
    set p p + 1 
  ]
  set materialout matrix:from-row-list [ [0] [0] [0] [0] [0] ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to companies-buy-raw-materials
; only biodiesel companies currently have significant other input costs from raw materials.
  ask companies with [ color = blue ][
    set operatingvolume matrix:get materialin 2 0
;    show operatingvolume / materialmaxin
    let rawmaterialfraction matrix:get materialinprofile 0 0
    let totalrawmaterial operatingvolume * rawmaterialfraction
    let mcost totalrawmaterial * unitrawmaterialcost
    matrix:set materialin 0 0 totalrawmaterial
    set money money - mcost  
  ]
  ask companies with [ color = red ][
    set operatingvolume matrix:get materialin 1 0
;    show operatingvolume / materialmaxin
  ]
  ask companies with [ color = black ][
    set operatingvolume matrix:get materialin 1 0
;    show operatingvolume / materialmaxin
  ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to update-finances
  set money money - operatingcosts
  let loanpayment 0
  set loanpayment initialloan * interestrate / ( 1 - (1 + interestrate ) ^ ( - 20 ) )
   if loan > 0 [
     set money money - loanpayment / 365
     set loan loan + interestrate * loan / 365 - loanpayment / 365
   ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to calculate-profit
  set profit money - moneyold
  set moneyold money
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to go-bust
  if money < 0 [ die ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to get-older
  set age age + 1
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; New friends are created everytime the time counter reaches 3.  It is then resest to 0.
to make-new-friends
  if contact-rate > 0 [
    let inv-contact-rate int 30 / contact-rate
    if ticks mod inv-contact-rate = 0 [
      create-friend-with one-of foodprocessors
      ask friends [ set color red ]
    ]
  ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to create-new-companies
  let solidwasteleft matrix:get materialtolandfill 1 0 
;  show solidwasteleft
  if ticks mod company-creation-rate = 1 [
    if solidwasteleft > 200 [
; anaerobic digester      
      let composter-or-AD random 10 / 10
      let financial-incentive-for-AD 0.5
      ifelse financial-incentive-for-AD > composter-or-AD [
        let companysize 150
        let profitability 0
           if solidwasteleft > 1500 [
             if ROCs-incentive > 4 [
             set profitability 1 
             ]
           ]
           if profitability = 1 [ 
             create-companies 1 [ 
             set materialmaxin companysize
             set size materialmaxin / 200
             set materialinprofile matrix:from-row-list [[0] [1] [0] [0] [0]]
             set materialneed matrix:times-scalar materialinprofile materialmaxin
             set materialin matrix:from-row-list [ [0] [0] [0] [0] [0] ]
;             set process matrix:from-row-list [ [0 5 0 0 0] [0 0 0 0 0] [0 0 0 0 0] [0 1.4 0 0 0] [0 0 0 0 0] ] 
        set process matrix:from-row-list [ [0 1 0 0 0] [0 0 0 0 0] [0 0 0 0 0] [0 1.4 0 0 0] [0 0 0 0 0] ] 
             set unitrawmaterialcost 0 
             set unitproductvalue matrix:from-row-list [[1] [0] [0] [0] [0]]
             matrix:set unitproductvalue 0 0 ( 9.6 * ROCs-incentive + 0.04 * 50)
             matrix:set unitproductvalue 3 0 ( -1 * CO2-tax )
;             set operatingcosts 420 * companysize
             set operatingcosts 80 * companysize
             set initialloan 50000 * companysize
             set loan initialloan
             set money initialloan
             set moneyold money
             set profit 0
             set contractlength random 180
             set color red
             set age 0
             let xval random 12 + 5
             let yval random -12 - 5
             setxy xval yval
             create-friends-with n-of initial-contacts foodprocessors
             ask friends [ set color red ]
             ]        
         ]          
      ]
      [
        create-companies 1 [ 
; composter          
        set materialmaxin random 200
        set size materialmaxin / 200
        set materialinprofile matrix:from-row-list [[0] [1] [0] [0] [0]]
        set materialin matrix:from-row-list [ [0] [0] [0] [0] [0] ]
        set materialneed matrix:times-scalar materialinprofile materialmaxin
  ;      show materialneed
        set process matrix:from-row-list [ [0 0.55 0 0 0] [0 0 0 0 0] [0 0 0 0 0] [0 0.324 0 0 0] [0 0 0 0 0]]
        set unitrawmaterialcost 0 
        set unitproductvalue matrix:from-row-list [[20] [0] [0] [-1] [0]] 
        matrix:set unitproductvalue 3 0 ( - CO2-tax )
        set color black
        set operatingcosts 5 * materialmaxin 
        set initialloan 500 * materialmaxin
        set loan initialloan
        set money initialloan
        set moneyold money
        set profit 0
        set contractlength 4
        set age 0
        setxy 5 -12
        let xval random -12 - 5
        let yval random -12 - 5
        setxy xval yval
        create-friends-with n-of initial-contacts foodprocessors
        ask friends [ set color red ]
        ] 
      ]
    ]
  ]
  let oilwasteleft matrix:get materialtolandfill 2 0 
  if ticks mod company-creation-rate = 1 [
; biodiesel
  let profitability 0
    if oilwasteleft > 800 [
      if mean [ matrix:get unitcontractvalue 2 0 ] of foodprocessors < 600 [
      set profitability 1 
      ]
  ]
    if profitability = 1 [
      create-companies 1 [ 
      set materialmaxin 600
      set size materialmaxin / 400
      set materialinprofile matrix:from-row-list [[0.13] [0] [1] [0] [0]]
      set materialin matrix:from-row-list [ [0] [0] [0] [0] [0] ]
      set materialneed matrix:times-scalar materialinprofile materialmaxin
      set process matrix:from-row-list [ [0 0 0.95 0 0] [0 0 0 0 0] [0 0 0 0 0] [0 0 0 0 0] [0 0 0.18 0 0]] 
      set unitrawmaterialcost 0.5 
      set unitproductvalue matrix:from-row-list [[644] [0] [0] [0] [-64]]
      matrix:set unitproductvalue 4 0 ( - landfillprice )
      set color blue 
      set operatingcosts materialmaxin * 0.05
      set initialloan 10000
      set loan initialloan
      set money initialloan
      set moneyold money
      set profit 0
      set age 0
      set contractlength 720
      let xval random 12 + 5
      let yval random 12 + 5
      setxy xval yval 
      create-friends-with n-of initial-contacts foodprocessors
      ask friends [ set color red ]
      ]
    ]
  ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Contracts only exist for a finite length of time, so update-contract-details updates the length
; of time remaining for each contract and terminates any that have come to the end of their natural
; life.
to update-contract-details
  let contractending nobody
  let contractvalue 0
  ask contracts [ set lengthofcontract lengthofcontract - 1 ] 
  set contractending contracts with [ lengthofcontract = 0 ] 
  ask contractending [ die ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to update-prices
   matrix:set unitproductvalue 3 0 ( - CO2-tax )
   matrix:set unitproductvalue 4 0 ( - landfillprice )
      if color = red [
      matrix:set unitproductvalue 0 0 ( 9.6 * ROCs-incentive + 0.04 * 50)
      ]
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to printoutinfo
  show materialmaxin
  show materialin
  show materialneed
  show materialout
  show materialinprofile
  show process
  show money
  show ethos
  show unitproductvalue
end
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
to makeplots
  set-current-plot "Waste to landfill"
    set-current-plot-pen "Organic waste"
    let solidwaste matrix:get materialtolandfill 1 0
    plot solidwaste 
;    set-current-plot-pen "Oil"
;    plot matrix:get materialtolandfill 2 0
;    set-current-plot-pen "Gas"
;    plot matrix:get materialtolandfill 3 0
;    set-current-plot-pen "Other"
;    plot matrix:get materialtolandfill 4 0
  set-current-plot "Organic waste price"
    set-current-plot-pen "Solid"
    histogram [ matrix:get unitcontractvalue 1 0 ] of foodprocessors
  set-current-plot "Waste oil price"
    set-current-plot-pen "Oil"
    histogram [ matrix:get unitcontractvalue 2 0 ] of foodprocessors
 set-current-plot "Composter profit"
    set-current-plot-pen "Profit"
    histogram [ profit ] of companies with [ color = black]
 set-current-plot "AD profit"
    set-current-plot-pen "Profit"
    histogram [ profit ] of companies with [ color = red ]
 set-current-plot "Mean organic price"
    set-current-plot-pen "Mean organic price"
    plot mean [ matrix:get unitcontractvalue 1 0 ] of foodprocessors
 set-current-plot "Number of firms"
    set-current-plot-pen "Composters"
    plot count companies with [ color = black ]
     set-current-plot-pen "ADplants"
    plot count companies with [ color = red ]
set-current-plot "Age of firms"
    set-current-plot-pen "Composters"
    histogram [ age ] of companies with [ color = black ]
    set-current-plot-pen "ADplants"
    histogram [ age ] of companies with [ color = red ]
set-current-plot "Employees"
    set-current-plot-pen "Composters"
    plot sum [ 0.025 * operatingvolume ] of companies with [ color = black ]
    set-current-plot-pen "ADplants"
    plot sum [ 0.05 * operatingvolume ] of companies with [ color = red ] 
;      set meanwastevalue 
;  plot moneyend - moneystart
;  set-current-plot-pen "Number of contracts"
;  plot count contracts
end
@#$#@#$#@
GRAPHICS-WINDOW
399
10
827
459
20
20
10.2
1
10
1
1
1
0
0
0
1
-20
20
-20
20
0
0
1
day
30.0

BUTTON
13
10
86
43
setup
setup
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
97
10
187
43
go once
go
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

BUTTON
195
11
300
44
go forever
go
T
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

PLOT
37
249
355
442
Waste to landfill
Time
Waste
0.0
10000.0
0.0
9000.0
true
true
"" ""
PENS
"Organic waste" 1.0 0 -16777216 true "" ""

PLOT
886
171
1046
313
Waste oil price
Price
No of firms
500.0
700.0
0.0
10.0
true
false
"" ""
PENS
"Oil" 1.0 1 -13345367 true "" ""

SLIDER
145
59
364
92
number-of-wasteproducers
number-of-wasteproducers
20
1000
25
1
1
NIL
HORIZONTAL

PLOT
886
16
1046
160
Organic waste price
Price
No of firms
-100.0
100.0
0.0
10.0
true
false
"" ""
PENS
"Solid" 1.0 1 -16777216 true "" ""

SLIDER
191
101
363
134
landfillprice
landfillprice
0
100
39
1
1
NIL
HORIZONTAL

INPUTBOX
13
50
137
110
company-creation-rate
14
1
0
Number

SLIDER
191
181
363
214
ROs-incentive
ROs-incentive
0
20
5
1
1
NIL
HORIZONTAL

PLOT
868
324
1068
474
Composter profit
Profit
No of firms
-20000.0
20000.0
0.0
5.0
true
false
"" ""
PENS
"profit" 100.0 1 -16777216 true "" ""

PLOT
867
486
1067
636
AD profit
Profit
No of firms
-20000.0
20000.0
0.0
5.0
true
false
"" ""
PENS
"profit" 1000.0 1 -2674135 true "" ""

SLIDER
191
141
363
174
Carbon-trading
Carbon-trading
0
1
0
1
1
NIL
HORIZONTAL

SLIDER
8
162
180
195
contact-rate
contact-rate
0
30
2
1
1
NIL
HORIZONTAL

SLIDER
8
120
180
153
initial-contacts
initial-contacts
0
20
4
1
1
NIL
HORIZONTAL

PLOT
5
484
205
634
Mean organic price
Time
Price
0.0
10000.0
-50.0
60.0
true
false
"" ""
PENS
"Mean organic price" 1.0 0 -16777216 true "" ""

PLOT
216
484
416
634
Number of firms
Tme
Number
0.0
10000.0
0.0
60.0
true
false
"" ""
PENS
"Composters" 1.0 0 -16777216 true "" ""
"ADplants" 1.0 0 -2674135 true "" ""

PLOT
435
485
635
635
Age of firms
Age
Number
0.0
3000.0
0.0
20.0
true
false
"" ""
PENS
"Composters" 365.0 1 -16777216 true "" ""
"ADplants" 365.0 1 -2674135 true "" ""

PLOT
652
486
852
636
Employees
Time
NIL
0.0
10000.0
0.0
400.0
true
false
"" ""
PENS
"Composters" 1.0 0 -16777216 true "" ""
"ADplants" 1.0 0 -2674135 true "" ""

BUTTON
30
208
127
241
NIL
make-movie
NIL
1
T
OBSERVER
NIL
NIL
NIL
NIL
1

@#$#@#$#@
## WHAT IS IT?

This is version 3-4 of the development of an ABM modelling some issues related to the Humber Region.  The aim is to investigate the impact of different policies and different policy combinations on the development of a bio-waste industry.

The initial idea is to focus on two particular policies, namely, the funding of facilitators to help create symbiotic links between companies and the role of a financial instrument.

## TERMS OF USE

This model is licensed under a Creative Commons Licence:  see

[Creative Commons Licence](http://creativecommons.org/licenses/by-nd/3.0/)

   
## HOW IT WORKS

In this ABM model, agents are companies.  These companies have a contact network, where a contact is another company that they are aware of but have no formal contract with.  They also have a network of contracts with companies either a contract to supply material to another company or a contract to obtain material from another company.  In the simulation, the network of friends is shown in red and the network of contracts is shown in either blue or black - black means that the contract is for organic waste, blue is for waste oil.

There are two breeds of agent:  
1. Suppliers of waste. At the moment, these are modelled on food processors and are shown in green.  They produce that waste oil and organic waste.  
2. Waste processors.  There are three types of these
 a) Biodiesel plants (blue) that process waste oil.
 b) Anaerobic digestion plants (red) that process organic waste.
 c) Composters (black) that process organic waste.

At the start, there are only suppliers of waste present.  The waste processors are created as a consequence of waste being available.

In each timestep, the following happens:  
1. The food processors produce waste.  
2. Food processors supply any existing waste contracts that they have. Finances are updated accordingly.  
3. New waste contracts are made and finances updated accordingly.  
4. Food processors update the price that they pay to get rid of waste: if they have managed to get rid of all their waste, then they decrease the price they pay, if they have waste left, then they increase the price they pay.  
5. Finances of food processors are updated - they receive money for the products that they develop and pay money to get rid of all remaining waste to landfill.  
6. Companies that process waste, process any waste that they have received.  
7. Finances of waste producing companies are updated.  
8. Any company that has no money goes bust.  
9. New contacts are made - how often depends on the contacts-rate slider.  
10. New waste processors are created - how often depends on the slider called company-creation-rate.  Biodiesel plants are created providing there is  
enough waste oil around.  Either composters or AD plants are started (50/50) depending on whether there is solid waste and, for AD plants, only if a "profitabilty" measure is satisfied.  
11. Update any contract details - end any contracts that have reached the end of their contract period.

## HOW TO USE IT

i) Click on "set up".  
ii) Then click on "go", to make the model take one time step.  
iii) Or click on "go forever" to make the model keep running.

## THINGS TO NOTICE

## THINGS TO TRY

It is currently set up so that the number of companies and the frequency that new friends are made can be changed by moving the sliders. Please see the accompanying worksheet (ABMworksheet.pdf) for some things to try.

## NETLOGO FEATURES

## RELATED MODELS

## CREDITS AND REFERENCES

This version of the model was coded by Anne Skeldon and was developed by discussions with Tina Balke, Lauren Basson, Nigel Gilbert, Paul Jensen, Ozge Dilaver Kalkan, Michelle Grant, Alex Penn, Frank Schiller, Lauren Basson, Nigel Gilbert and Aidong Yang. 
@#$#@#$#@
default
true
0
Polygon -7500403 true true 150 5 40 250 150 205 260 250

airplane
true
0
Polygon -7500403 true true 150 0 135 15 120 60 120 105 15 165 15 195 120 180 135 240 105 270 120 285 150 270 180 285 210 270 165 240 180 180 285 195 285 165 180 105 180 60 165 15

arrow
true
0
Polygon -7500403 true true 150 0 0 150 105 150 105 293 195 293 195 150 300 150

box
false
0
Polygon -7500403 true true 150 285 285 225 285 75 150 135
Polygon -7500403 true true 150 135 15 75 150 15 285 75
Polygon -7500403 true true 15 75 15 225 150 285 150 135
Line -16777216 false 150 285 150 135
Line -16777216 false 150 135 15 75
Line -16777216 false 150 135 285 75

bug
true
0
Circle -7500403 true true 96 182 108
Circle -7500403 true true 110 127 80
Circle -7500403 true true 110 75 80
Line -7500403 true 150 100 80 30
Line -7500403 true 150 100 220 30

butterfly
true
0
Polygon -7500403 true true 150 165 209 199 225 225 225 255 195 270 165 255 150 240
Polygon -7500403 true true 150 165 89 198 75 225 75 255 105 270 135 255 150 240
Polygon -7500403 true true 139 148 100 105 55 90 25 90 10 105 10 135 25 180 40 195 85 194 139 163
Polygon -7500403 true true 162 150 200 105 245 90 275 90 290 105 290 135 275 180 260 195 215 195 162 165
Polygon -16777216 true false 150 255 135 225 120 150 135 120 150 105 165 120 180 150 165 225
Circle -16777216 true false 135 90 30
Line -16777216 false 150 105 195 60
Line -16777216 false 150 105 105 60

car
false
0
Polygon -7500403 true true 300 180 279 164 261 144 240 135 226 132 213 106 203 84 185 63 159 50 135 50 75 60 0 150 0 165 0 225 300 225 300 180
Circle -16777216 true false 180 180 90
Circle -16777216 true false 30 180 90
Polygon -16777216 true false 162 80 132 78 134 135 209 135 194 105 189 96 180 89
Circle -7500403 true true 47 195 58
Circle -7500403 true true 195 195 58

circle
false
0
Circle -7500403 true true 0 0 300

circle 2
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240

cow
false
0
Polygon -7500403 true true 200 193 197 249 179 249 177 196 166 187 140 189 93 191 78 179 72 211 49 209 48 181 37 149 25 120 25 89 45 72 103 84 179 75 198 76 252 64 272 81 293 103 285 121 255 121 242 118 224 167
Polygon -7500403 true true 73 210 86 251 62 249 48 208
Polygon -7500403 true true 25 114 16 195 9 204 23 213 25 200 39 123

cylinder
false
0
Circle -7500403 true true 0 0 300

dot
false
0
Circle -7500403 true true 90 90 120

face happy
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 255 90 239 62 213 47 191 67 179 90 203 109 218 150 225 192 218 210 203 227 181 251 194 236 217 212 240

face neutral
false
0
Circle -7500403 true true 8 7 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Rectangle -16777216 true false 60 195 240 225

face sad
false
0
Circle -7500403 true true 8 8 285
Circle -16777216 true false 60 75 60
Circle -16777216 true false 180 75 60
Polygon -16777216 true false 150 168 90 184 62 210 47 232 67 244 90 220 109 205 150 198 192 205 210 220 227 242 251 229 236 206 212 183

fish
false
0
Polygon -1 true false 44 131 21 87 15 86 0 120 15 150 0 180 13 214 20 212 45 166
Polygon -1 true false 135 195 119 235 95 218 76 210 46 204 60 165
Polygon -1 true false 75 45 83 77 71 103 86 114 166 78 135 60
Polygon -7500403 true true 30 136 151 77 226 81 280 119 292 146 292 160 287 170 270 195 195 210 151 212 30 166
Circle -16777216 true false 215 106 30

flag
false
0
Rectangle -7500403 true true 60 15 75 300
Polygon -7500403 true true 90 150 270 90 90 30
Line -7500403 true 75 135 90 135
Line -7500403 true 75 45 90 45

flower
false
0
Polygon -10899396 true false 135 120 165 165 180 210 180 240 150 300 165 300 195 240 195 195 165 135
Circle -7500403 true true 85 132 38
Circle -7500403 true true 130 147 38
Circle -7500403 true true 192 85 38
Circle -7500403 true true 85 40 38
Circle -7500403 true true 177 40 38
Circle -7500403 true true 177 132 38
Circle -7500403 true true 70 85 38
Circle -7500403 true true 130 25 38
Circle -7500403 true true 96 51 108
Circle -16777216 true false 113 68 74
Polygon -10899396 true false 189 233 219 188 249 173 279 188 234 218
Polygon -10899396 true false 180 255 150 210 105 210 75 240 135 240

house
false
0
Rectangle -7500403 true true 45 120 255 285
Rectangle -16777216 true false 120 210 180 285
Polygon -7500403 true true 15 120 150 15 285 120
Line -16777216 false 30 120 270 120

leaf
false
0
Polygon -7500403 true true 150 210 135 195 120 210 60 210 30 195 60 180 60 165 15 135 30 120 15 105 40 104 45 90 60 90 90 105 105 120 120 120 105 60 120 60 135 30 150 15 165 30 180 60 195 60 180 120 195 120 210 105 240 90 255 90 263 104 285 105 270 120 285 135 240 165 240 180 270 195 240 210 180 210 165 195
Polygon -7500403 true true 135 195 135 240 120 255 105 255 105 285 135 285 165 240 165 195

line
true
0
Line -7500403 true 150 0 150 300

line half
true
0
Line -7500403 true 150 0 150 150

pentagon
false
0
Polygon -7500403 true true 150 15 15 120 60 285 240 285 285 120

person
false
0
Circle -7500403 true true 110 5 80
Polygon -7500403 true true 105 90 120 195 90 285 105 300 135 300 150 225 165 300 195 300 210 285 180 195 195 90
Rectangle -7500403 true true 127 79 172 94
Polygon -7500403 true true 195 90 240 150 225 180 165 105
Polygon -7500403 true true 105 90 60 150 75 180 135 105

plant
false
0
Rectangle -7500403 true true 135 90 165 300
Polygon -7500403 true true 135 255 90 210 45 195 75 255 135 285
Polygon -7500403 true true 165 255 210 210 255 195 225 255 165 285
Polygon -7500403 true true 135 180 90 135 45 120 75 180 135 210
Polygon -7500403 true true 165 180 165 210 225 180 255 120 210 135
Polygon -7500403 true true 135 105 90 60 45 45 75 105 135 135
Polygon -7500403 true true 165 105 165 135 225 105 255 45 210 60
Polygon -7500403 true true 135 90 120 45 150 15 180 45 165 90

sheep
false
0
Rectangle -7500403 true true 151 225 180 285
Rectangle -7500403 true true 47 225 75 285
Rectangle -7500403 true true 15 75 210 225
Circle -7500403 true true 135 75 150
Circle -16777216 true false 165 76 116

square
false
0
Rectangle -7500403 true true 30 30 270 270

square 2
false
0
Rectangle -7500403 true true 30 30 270 270
Rectangle -16777216 true false 60 60 240 240

star
false
0
Polygon -7500403 true true 151 1 185 108 298 108 207 175 242 282 151 216 59 282 94 175 3 108 116 108

target
false
0
Circle -7500403 true true 0 0 300
Circle -16777216 true false 30 30 240
Circle -7500403 true true 60 60 180
Circle -16777216 true false 90 90 120
Circle -7500403 true true 120 120 60

tree
false
0
Circle -7500403 true true 118 3 94
Rectangle -6459832 true false 120 195 180 300
Circle -7500403 true true 65 21 108
Circle -7500403 true true 116 41 127
Circle -7500403 true true 45 90 120
Circle -7500403 true true 104 74 152

triangle
false
0
Polygon -7500403 true true 150 30 15 255 285 255

triangle 2
false
0
Polygon -7500403 true true 150 30 15 255 285 255
Polygon -16777216 true false 151 99 225 223 75 224

truck
false
0
Rectangle -7500403 true true 4 45 195 187
Polygon -7500403 true true 296 193 296 150 259 134 244 104 208 104 207 194
Rectangle -1 true false 195 60 195 105
Polygon -16777216 true false 238 112 252 141 219 141 218 112
Circle -16777216 true false 234 174 42
Rectangle -7500403 true true 181 185 214 194
Circle -16777216 true false 144 174 42
Circle -16777216 true false 24 174 42
Circle -7500403 false true 24 174 42
Circle -7500403 false true 144 174 42
Circle -7500403 false true 234 174 42

turtle
true
0
Polygon -10899396 true false 215 204 240 233 246 254 228 266 215 252 193 210
Polygon -10899396 true false 195 90 225 75 245 75 260 89 269 108 261 124 240 105 225 105 210 105
Polygon -10899396 true false 105 90 75 75 55 75 40 89 31 108 39 124 60 105 75 105 90 105
Polygon -10899396 true false 132 85 134 64 107 51 108 17 150 2 192 18 192 52 169 65 172 87
Polygon -10899396 true false 85 204 60 233 54 254 72 266 85 252 107 210
Polygon -7500403 true true 119 75 179 75 209 101 224 135 220 225 175 261 128 261 81 224 74 135 88 99

wheel
false
0
Circle -7500403 true true 3 3 294
Circle -16777216 true false 30 30 240
Line -7500403 true 150 285 150 15
Line -7500403 true 15 150 285 150
Circle -7500403 true true 120 120 60
Line -7500403 true 216 40 79 269
Line -7500403 true 40 84 269 221
Line -7500403 true 40 216 269 79
Line -7500403 true 84 40 221 269

x
false
0
Polygon -7500403 true true 270 75 225 30 30 225 75 270
Polygon -7500403 true true 30 75 75 30 270 225 225 270

@#$#@#$#@
NetLogo 5.0.5
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
@#$#@#$#@
default
0.0
-0.2 0 0.0 1.0
0.0 1 1.0 0.0
0.2 0 0.0 1.0
link direction
true
0
Line -7500403 true 150 150 90 180
Line -7500403 true 150 150 210 180

@#$#@#$#@
0
@#$#@#$#@
